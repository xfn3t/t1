package ru.homework.microservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.homework.microservice.common.AccountStatus;
import ru.homework.microservice.common.ClientStatus;
import ru.homework.microservice.common.TransactionStatus;
import ru.homework.microservice.dto.request.AccountRequestDto;
import ru.homework.microservice.dto.request.ClientRequestDto;
import ru.homework.microservice.dto.request.TransactionRequestDto;
import ru.homework.microservice.dto.request.UserRequestDto;
import ru.homework.microservice.dto.response.AccountResponseDto;
import ru.homework.microservice.dto.response.ClientResponseDto;
import ru.homework.microservice.dto.response.ClientStatusResponse;
import ru.homework.microservice.dto.response.TransactionResponseDto;
import ru.homework.microservice.dto.response.UserResponseDto;
import ru.homework.microservice.service.clients.BlacklistClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.liquibase.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "transactions.reject.threshold=3",
                "security.enabled=false"
        })
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class TransactionControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlacklistClient blacklistClient;

    @Autowired
    private MockMvc mockMvc;

    private Long clientInternalId;
    private UUID clientPublicId;
    private Long accountInternalId;
    private String accountPublicId;

    @BeforeEach
    void setup() {
        // Отключаем CSRF для всех запросов
        rest.getRestTemplate().getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("X-Requested-With", "XMLHttpRequest");
            return execution.execute(request, body);
        });

        // Создаем клиента
        ClientRequestDto clientReq = new ClientRequestDto();
        clientReq.setFirstName("John");
        clientReq.setMiddleName("M");
        clientReq.setLastName("Doe");
        ResponseEntity<ClientResponseDto> clientResp = rest.postForEntity(
                createURL("/api/clients"), clientReq, ClientResponseDto.class);

        assertThat(clientResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        clientInternalId = clientResp.getBody().getId();
        clientPublicId = clientResp.getBody().getClientId();

        // Настраиваем заглушку для blacklistClient
        when(blacklistClient.getStatus(clientPublicId.toString()))
                .thenReturn(new ClientStatusResponse(clientPublicId.toString(), ClientStatus.OK));

        // Создаем счет
        AccountRequestDto accReq = new AccountRequestDto();
        accReq.setClientId(clientInternalId);
        accReq.setInitialBalance(new BigDecimal("1000"));
        ResponseEntity<AccountResponseDto> accResp = rest.postForEntity(
                createURL("/api/accounts"), accReq, AccountResponseDto.class);

        assertThat(accResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        accountInternalId = accResp.getBody().getId();
        accountPublicId = accResp.getBody().getAccountId();
    }

    @Test
    void testSuccessfulTransaction() throws Exception {
        // Выполняем транзакцию
        TransactionRequestDto txReq = new TransactionRequestDto();
        txReq.setAccountId(accountPublicId);
        txReq.setAmount(new BigDecimal("200"));
        ResponseEntity<TransactionResponseDto> txResp = rest.postForEntity(
                createURL("/api/transactions"), txReq, TransactionResponseDto.class);

        assertThat(txResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(txResp.getBody().getStatus()).isEqualTo(TransactionStatus.REQUESTED);
        assertThat(txResp.getBody().getAmount()).isEqualByComparingTo("200");

        // Проверяем баланс счета
        ResponseEntity<AccountResponseDto> accountResp = rest.getForEntity(
                createURL("/api/accounts/" + accountInternalId), AccountResponseDto.class);

        assertThat(accountResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accountResp.getBody().getBalance()).isEqualByComparingTo("800");
    }

    @Test
    void testInsufficientFunds() {
        TransactionRequestDto txReq = new TransactionRequestDto();
        txReq.setAccountId(accountPublicId);
        txReq.setAmount(new BigDecimal("1500"));

        ResponseEntity<String> response = rest.postForEntity(
                createURL("/api/transactions"), txReq, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Insufficient funds");
    }

    @Test
    void testBlacklistedClient() {
        // Настраиваем клиента в черном списке
        when(blacklistClient.getStatus(clientPublicId.toString()))
                .thenReturn(new ClientStatusResponse(clientPublicId.toString(), ClientStatus.BLACKLISTED));

        // Первая транзакция (должна заблокировать счет)
        performAndVerifyRejectedTransaction("100", AccountStatus.BLOCKED);

        // Вторая транзакция (счет остается заблокированным)
        performAndVerifyRejectedTransaction("200", AccountStatus.BLOCKED);

        // Третья транзакция (должна арестовать счет)
        performAndVerifyRejectedTransaction("300", AccountStatus.ARRESTED);
    }

    private void performAndVerifyRejectedTransaction(String amount, AccountStatus expectedStatus) {
        TransactionRequestDto txReq = new TransactionRequestDto();
        txReq.setAccountId(accountPublicId);
        txReq.setAmount(new BigDecimal(amount));

        ResponseEntity<TransactionResponseDto> txResp = rest.postForEntity(
                createURL("/api/transactions"), txReq, TransactionResponseDto.class);

        assertThat(txResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(txResp.getBody().getStatus()).isEqualTo(TransactionStatus.REJECTED);

        // Проверяем статус счета
        ResponseEntity<AccountResponseDto> accountResp = rest.getForEntity(
                createURL("/api/accounts/" + accountInternalId), AccountResponseDto.class);

        assertThat(accountResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accountResp.getBody().getStatus()).isEqualTo(expectedStatus);
    }

    @Test
    void testGetTransactionById() {
        // Создаем транзакцию
        TransactionRequestDto txReq = new TransactionRequestDto();
        txReq.setAccountId(accountPublicId);
        txReq.setAmount(new BigDecimal("300"));
        ResponseEntity<TransactionResponseDto> createResp = rest.postForEntity(
                createURL("/api/transactions"), txReq, TransactionResponseDto.class);

        String txId = createResp.getBody().getTransactionId();

        // Получаем транзакцию по ID
        ResponseEntity<TransactionResponseDto> getResp = rest.getForEntity(
                createURL("/api/transactions/" + txId), TransactionResponseDto.class);

        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().getTransactionId()).isEqualTo(txId);
        assertThat(getResp.getBody().getAmount()).isEqualByComparingTo("300");
    }

    @Test
    void testGetTransactionsByAccount() {
        // Создаем несколько транзакций
        performTransaction("100");
        performTransaction("200");
        performTransaction("300");

        // Получаем транзакции по счету
        ResponseEntity<TransactionResponseDto[]> response = rest.getForEntity(
                createURL("/api/transactions?accountId=" + accountPublicId),
                TransactionResponseDto[].class);

        TransactionResponseDto[] transactions = response.getBody();
        assertThat(transactions).hasSize(3);

        // Проверяем сортировку по времени
        assertThat(transactions[0].getAmount()).isEqualByComparingTo("300");
        assertThat(transactions[1].getAmount()).isEqualByComparingTo("200");
        assertThat(transactions[2].getAmount()).isEqualByComparingTo("100");
    }

    @Test
    void testUserHeavyOperationPerformance() {
        // Создаем пользователя
        UserRequestDto userReq = new UserRequestDto();
        userReq.setUsername("perfuser");
        userReq.setEmail("perf@test.com");
        ResponseEntity<UserResponseDto> userResp = rest.postForEntity(
                createURL("/api/users"), userReq, UserResponseDto.class);

        assertThat(userResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Замеряем время выполнения
        long startTime = System.currentTimeMillis();
        ResponseEntity<Void> response = rest.postForEntity(
                createURL("/api/users/heavy"), null, Void.class);
        long duration = System.currentTimeMillis() - startTime;

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(duration).isGreaterThanOrEqualTo(600);
    }

    @Test
    void testValidationErrorsWithMockMvc() throws Exception {

        UserRequestDto invalidUser = new UserRequestDto();
        invalidUser.setUsername("valid");
        invalidUser.setEmail("invalid-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')].message")
                        .value("Email должен быть корректным"));
    }

    private void performTransaction(String amount) {
        TransactionRequestDto txReq = new TransactionRequestDto();
        txReq.setAccountId(accountPublicId);
        txReq.setAmount(new BigDecimal(amount));
        rest.postForEntity(createURL("/api/transactions"), txReq, TransactionResponseDto.class);
    }


    private String createURL(String uri) {
        return "http://localhost:" + port + uri;
    }
}
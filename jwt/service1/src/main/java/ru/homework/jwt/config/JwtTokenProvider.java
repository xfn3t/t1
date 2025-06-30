package ru.homework.jwt.config;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${security.jwt.secret}") String secret;
    @Value("${security.jwt.expiration-ms}") long expMs;
    SecretKey key;

    @PostConstruct
    void init() {
        key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    }

    public String createToken(String clientId) {
        try {
            JWSSigner signer = new MACSigner(key);
            var now = Instant.now();
            var claims = new JWTClaimsSet.Builder()
                    .subject(clientId)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusMillis(expMs)))
                    .build();
            var jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256).build(),
                    claims);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}

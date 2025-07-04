package ru.t1.homework.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, updatable = false, unique = true)
    private UUID clientId;

    private String firstName;

    private String middleName;

    private String lastName;

    @PrePersist
    public void ensureClientId() {
        if (this.clientId == null) {
            this.clientId = UUID.randomUUID();
        }
    }
}
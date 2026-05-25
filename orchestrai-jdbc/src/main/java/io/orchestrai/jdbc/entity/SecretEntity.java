package io.orchestrai.jdbc.entity;

import java.time.Instant;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "secrets")
public class SecretEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "namespace", nullable = false, length = 100)
    public String namespace;

    @Column(name = "key", nullable = false, length = 100)
    public String secretKey;

    @Column(name = "encrypted_value", nullable = false, columnDefinition = "TEXT")
    public String encryptedValue;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}

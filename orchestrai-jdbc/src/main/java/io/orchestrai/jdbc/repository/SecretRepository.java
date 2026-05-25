package io.orchestrai.jdbc.repository;

import java.util.Optional;
import java.util.UUID;

import io.orchestrai.jdbc.entity.SecretEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SecretRepository implements PanacheRepositoryBase<SecretEntity, UUID> {

    public Optional<SecretEntity> findByNamespaceAndKey(String namespace, String key) {
        return find("namespace = ?1 and secretKey = ?2", namespace, key).firstResultOptional();
    }
}

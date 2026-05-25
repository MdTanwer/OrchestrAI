package io.orchestrai.jdbc.repository;

import java.util.List;
import java.util.UUID;

import io.orchestrai.jdbc.entity.TriggerEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TriggerRepository implements PanacheRepositoryBase<TriggerEntity, UUID> {

    public List<TriggerEntity> findByFlowRefId(UUID flowRefId) {
        return list("flowRefId", flowRefId);
    }

    public List<TriggerEntity> findEnabledByType(String type) {
        return list("enabled = true and type = ?1", type);
    }
}

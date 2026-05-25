package io.orchestrai.jdbc.repository;

import java.util.List;
import java.util.UUID;

import io.orchestrai.core.enums.ExecutionState;
import io.orchestrai.jdbc.entity.ExecutionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExecutionRepository implements PanacheRepositoryBase<ExecutionEntity, UUID> {

    public List<ExecutionEntity> findByFlowRefId(UUID flowRefId) {
        return list("flowRefId", flowRefId);
    }

    public List<ExecutionEntity> findByState(ExecutionState state) {
        return list("state", state);
    }

    public List<ExecutionEntity> findByNamespace(String namespace) {
        return list("namespace", namespace);
    }
}

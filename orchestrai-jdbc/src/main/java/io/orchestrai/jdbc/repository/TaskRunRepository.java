package io.orchestrai.jdbc.repository;

import java.util.List;
import java.util.UUID;

import io.orchestrai.jdbc.entity.TaskRunEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TaskRunRepository implements PanacheRepositoryBase<TaskRunEntity, UUID> {

    public List<TaskRunEntity> findByExecutionId(UUID executionId) {
        return list("executionId", executionId);
    }
}

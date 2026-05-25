package io.orchestrai.jdbc.repository;

import java.util.List;
import java.util.UUID;

import io.orchestrai.jdbc.entity.LogEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogRepository implements PanacheRepositoryBase<LogEntity, Long> {

    public List<LogEntity> findByExecutionId(UUID executionId) {
        return list("executionId = ?1 order by createdAt asc", executionId);
    }

    public List<LogEntity> findByTaskRunId(UUID taskRunId) {
        return list("taskRunId = ?1 order by createdAt asc", taskRunId);
    }
}

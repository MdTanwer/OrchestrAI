package io.orchestrai.jdbc.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.orchestrai.jdbc.entity.FlowEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FlowRepository implements PanacheRepositoryBase<FlowEntity, UUID> {

    public Optional<FlowEntity> findByNamespaceFlowIdAndVersion(
            String namespace, String flowId, int version) {
        return find("namespace = ?1 and flowId = ?2 and version = ?3", namespace, flowId, version)
                .firstResultOptional();
    }

    public Optional<FlowEntity> findLatestVersion(String namespace, String flowId) {
        return find("namespace = ?1 and flowId = ?2 order by version desc", namespace, flowId)
                .firstResultOptional();
    }

    public List<FlowEntity> listByNamespace(String namespace) {
        return list("namespace", namespace);
    }
}

/**
 * TODO: Implement FlowEntity (Panache entity for {@code flows} table).
 *
 * <p><b>JSON / domain mapping contract</b> ({@link io.orchestrai.core.model.Flow}):
 * <ul>
 *   <li>{@code Flow.id} (Java) ↔ JSON field {@code uuid} ↔ DB column {@code flows.id} (UUID PK)</li>
 *   <li>{@code Flow.flowId} (Java) ↔ JSON/YAML field {@code id} ↔ DB column {@code flows.flow_id} (string)</li>
 * </ul>
 * Do not map {@code Flow.id} to {@code flow_id} or {@code Flow.flowId} to {@code id} — silent bugs.
 *
 * @see docs/06-data-models.md
 */
package io.orchestrai.jdbc.entity;

public class FlowEntity {
    // TODO: add implementation
}

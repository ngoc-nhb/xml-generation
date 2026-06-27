package com.company.xmlgen.masterdata.entity;

import com.company.xmlgen.common.persistence.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Persistence model for the {@code master_data_records} table.
 *
 * @see docs/03-database-design/03-database-design.md §4.7
 */
@Entity
@Table(
        name = "master_data_records",
        indexes = @Index(name = "idx_master_data_records_type_id", columnList = "master_data_type_id"))
public class MasterDataRecordEntity extends BaseEntity {

    @Column(name = "master_data_type_id", nullable = false)
    private Long masterDataTypeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_json", columnDefinition = "jsonb", nullable = false)
    private JsonNode dataJson;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected MasterDataRecordEntity() {
    }

    public MasterDataRecordEntity(Long masterDataTypeId, JsonNode dataJson) {
        this.masterDataTypeId = masterDataTypeId;
        this.dataJson = dataJson;
    }

    public Long getMasterDataTypeId() {
        return masterDataTypeId;
    }

    public void setMasterDataTypeId(Long masterDataTypeId) {
        this.masterDataTypeId = masterDataTypeId;
    }

    public JsonNode getDataJson() {
        return dataJson;
    }

    public void setDataJson(JsonNode dataJson) {
        this.dataJson = dataJson;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}

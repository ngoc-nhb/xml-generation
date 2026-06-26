package com.company.xmlgen.masterdata.entity;

import com.company.xmlgen.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Persistence model for the {@code master_data_types} table.
 *
 * @see docs/03-database-design/03-database-design.md §4.5
 */
@Entity
@Table(
        name = "master_data_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_master_data_types_code", columnNames = "code"),
        indexes = @Index(name = "idx_master_data_types_status", columnList = "status"))
public class MasterDataTypeEntity extends BaseEntity {

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MasterDataTypeStatus status;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected MasterDataTypeEntity() {
    }

    public MasterDataTypeEntity(String code, String name, MasterDataTypeStatus status) {
        this.code = code;
        this.name = name;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MasterDataTypeStatus getStatus() {
        return status;
    }

    public void setStatus(MasterDataTypeStatus status) {
        this.status = status;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}

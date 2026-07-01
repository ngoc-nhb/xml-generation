package com.company.xmlgen.workspace.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * Counts resources owned by a workspace for delete protection.
 *
 * <p>Uses native SQL so owned entities do not need {@code workspaceId} mapped in JPA yet.
 */
@Repository
public class WorkspaceOwnedResourceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public boolean hasOwnedResources(Long workspaceId) {
        return countTemplates(workspaceId) > 0
                || countMasterDataTypes(workspaceId) > 0
                || countSavedInputs(workspaceId) > 0
                || countExportHistories(workspaceId) > 0;
    }

    public long countTemplates(Long workspaceId) {
        return countFromTable("templates", workspaceId);
    }

    public long countMasterDataTypes(Long workspaceId) {
        return countFromTable("master_data_types", workspaceId);
    }

    public long countSavedInputs(Long workspaceId) {
        if (!tableExists("saved_inputs")) {
            return 0;
        }
        return countFromTable("saved_inputs", workspaceId);
    }

    public long countExportHistories(Long workspaceId) {
        if (!tableExists("export_histories")) {
            return 0;
        }
        return countFromTable("export_histories", workspaceId);
    }

    private long countFromTable(String tableName, Long workspaceId) {
        Number count = (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM " + tableName + " WHERE workspace_id = :workspaceId")
                .setParameter("workspaceId", workspaceId)
                .getSingleResult();
        return count.longValue();
    }

    private boolean tableExists(String tableName) {
        Number count = (Number) entityManager
                .createNativeQuery("""
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name = :tableName
                        """)
                .setParameter("tableName", tableName)
                .getSingleResult();
        return count.longValue() > 0;
    }
}

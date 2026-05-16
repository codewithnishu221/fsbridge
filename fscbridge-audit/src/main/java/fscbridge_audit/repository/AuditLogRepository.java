package fscbridge_audit.repository;

import fscbridge_audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {


    List<AuditLog> findByJobId(String jobId);

    List<AuditLog> findByJobIdAndAction(String jobId, String action);
    long countByJobIdAndActionAndSuccess(String jobId, String action, Boolean success);
}

package fscbridge_core.model;

import fscbridge_core.enums.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MigrationJob {

    @Builder.Default
    private String jobId = UUID.randomUUID().toString();
    private String jobName;
    private String sourceObject;
    private String targetObject;
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int totalRecords;
    private int successCount;
    private int failureCount;
    private List<SalesforceRecord> records;
    private List<FieldMapping> fieldMappings;
    private String failureReason;
    @Builder.Default
    private boolean dryRun = false;


}

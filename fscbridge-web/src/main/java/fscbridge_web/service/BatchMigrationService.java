package fscbridge_web.service;

import fscbridge_audit.service.AuditService;
import fscbridge_core.enums.JobStatus;
import fscbridge_core.model.MigrationJob;
import fscbridge_web.batch.MigrationJobConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class BatchMigrationService {

    private final JobLauncher jobLauncher;
    private final MigrationJobConfig migrationJobConfig;
    private final AuditService auditService;

    public MigrationJob runBatchMigration(MigrationJob job) {
        if (job.getJobId() == null) {
            job.setJobId(java.util.UUID.randomUUID().toString());
        }
        log.info("Launching Spring Batch migration for job: {}",
                job.getJobId());

        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        auditService.logJobStarted(job);

        try {
            org.springframework.batch.core.Job batchJob =
                    migrationJobConfig.createMigrationJob(
                            job.getSourceObject(),
                            job.getTargetObject(),
                            job.getJobId(),
                            job.isDryRun()
                    );

            JobParameters params = new JobParametersBuilder()
                    .addString("jobId", job.getJobId())
                    .addLong("startTime", System.currentTimeMillis())
                    .addString("sourceObject", job.getSourceObject())
                    .addString("targetObject", job.getTargetObject())
                    .addString("dryRun", String.valueOf(job.isDryRun()))
                    .toJobParameters();

           JobExecution execution = jobLauncher.run(batchJob, params);

            BatchStatus batchStatus = execution.getStatus();
            log.info("Spring Batch job completed with status: {}", batchStatus);

            if (batchStatus == BatchStatus.COMPLETED) {
                job.setStatus(JobStatus.COMPLETED);

                long successCount = auditService.getSuccessCount(job.getJobId());
                long failureCount = auditService.getFailureCount(job.getJobId());
                job.setSuccessCount((int) successCount);
                job.setFailureCount((int) failureCount);

            } else if (batchStatus == BatchStatus.FAILED) {
                job.setStatus(JobStatus.FAILED);
                job.setFailureReason("Spring Batch job failed. Check audit log.");

            } else {
                job.setStatus(JobStatus.FAILED);
                job.setFailureReason("Unexpected batch status: " + batchStatus);
            }

            job.setCompletedAt(LocalDateTime.now());
            auditService.logJobCompleted(job);

            return job;

        } catch (Exception e) {
            log.error("Batch migration failed for job {}: {}",
                    job.getJobId(), e.getMessage());

            job.setStatus(JobStatus.FAILED);
            job.setFailureReason(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            auditService.logJobFailed(job.getJobId(), e.getMessage());

            return job;
        }
    }
}
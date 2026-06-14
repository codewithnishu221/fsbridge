package fscbridge_web.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MigrationMetrics {

    private final Counter jobsStartedCounter;
    private final Counter jobsCompletedCounter;
    private final Counter jobsFailedCounter;
    private final Counter recordsSuccessCounter;
    private final Counter recordsFailedCounter;
    private final Counter dryRunsCounter;
    private final Counter rollbacksCounter;
    private final Counter validationsCounter;
    private final Timer migrationJobTimer;
    private final Timer validationTimer;

    public MigrationMetrics(MeterRegistry registry) {
        this.jobsStartedCounter = Counter.builder("migration.jobs.started")
                .description("Total number of migration jobs started")
                .tag("app", "fscbridge")
                .register(registry);

        this.jobsCompletedCounter = Counter.builder("migration.jobs.completed")
                .description("Total number of migration jobs completed successfully")
                .tag("app", "fscbridge")
                .register(registry);

        this.jobsFailedCounter = Counter.builder("migration.jobs.failed")
                .description("Total number of migration jobs that failed")
                .tag("app", "fscbridge")
                .register(registry);

        this.recordsSuccessCounter = Counter.builder("migration.records.success")
                .description("Total records migrated successfully across all jobs")
                .tag("app", "fscbridge")
                .register(registry);

        this.recordsFailedCounter = Counter.builder("migration.records.failed")
                .description("Total records that failed migration across all jobs")
                .tag("app", "fscbridge")
                .register(registry);

        this.dryRunsCounter = Counter.builder("migration.dryruns.total")
                .description("Total number of dry run migrations")
                .tag("app", "fscbridge")
                .register(registry);

        this.rollbacksCounter = Counter.builder("migration.rollbacks.total")
                .description("Total number of rollbacks triggered")
                .tag("app", "fscbridge")
                .register(registry);

        this.validationsCounter = Counter.builder("migration.validations.total")
                .description("Total number of pre-flight validations run")
                .tag("app", "fscbridge")
                .register(registry);

        this.migrationJobTimer = Timer.builder("migration.job.duration")
                .description("Time taken to complete a migration job")
                .tag("app", "fscbridge")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.validationTimer = Timer.builder("migration.validation.duration")
                .description("Time taken to run pre-flight validation")
                .tag("app", "fscbridge")
                .register(registry);

        log.info("MigrationMetrics registered successfully.");
    }

    public void recordJobStarted(boolean isDryRun) {
        jobsStartedCounter.increment();
        if (isDryRun) {
            dryRunsCounter.increment();
        }
        log.debug("Metric recorded: job started (dryRun={})", isDryRun);
    }

    public void recordJobCompleted(long durationMs,
                                    int successCount,
                                    int failureCount) {
        jobsCompletedCounter.increment();
        recordsSuccessCounter.increment(successCount);
        recordsFailedCounter.increment(failureCount);
        migrationJobTimer.record(durationMs, TimeUnit.MILLISECONDS);
        log.debug("Metric recorded: job completed in {}ms | " +
                "success={} failed={}", durationMs, successCount, failureCount);
    }

    public void recordJobFailed() {
        jobsFailedCounter.increment();
        log.debug("Metric recorded: job failed");
    }

    public void recordRollback() {
        rollbacksCounter.increment();
        log.debug("Metric recorded: rollback triggered");
    }

    public void recordValidation(long durationMs) {
        validationsCounter.increment();
        validationTimer.record(durationMs, TimeUnit.MILLISECONDS);
        log.debug("Metric recorded: validation completed in {}ms", durationMs);
    }

    public io.micrometer.core.instrument.Timer.Sample startTimer() {
        return Timer.start();
    }

    public void stopJobTimer(io.micrometer.core.instrument.Timer.Sample sample) {
        sample.stop(migrationJobTimer);
    }
}

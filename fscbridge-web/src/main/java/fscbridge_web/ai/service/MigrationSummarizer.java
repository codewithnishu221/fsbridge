package fscbridge_web.ai.service;

import fscbridge_core.model.MigrationJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationSummarizer {

    private final ChatModel chatModel;

    public String summarizeJob(MigrationJob job) {
        log.info("Generating AI summary for job: {}", job.getJobId());

        String systemPrompt = """
                You are a helpful data migration assistant for Salesforce.
                Generate a clear, professional summary of a migration job.

                Your summary must include:
                1. Overall result (success/partial/failure)
                2. Key statistics in plain English
                3. What the failures mean (if any)
                4. Specific action items to fix issues
                5. Recommendation for next steps

                Keep it under 200 words.
                Use bullet points for action items.
                Be specific and actionable, not generic.
                """;

        String successRate = job.getTotalRecords() > 0
                ? String.format("%.1f%%",
                (job.getSuccessCount() * 100.0 / job.getTotalRecords()))
                : "N/A";

        String userMessage = String.format("""
                Migration Job Summary:
                - Job Name: %s
                - Source Object: %s
                - Target Object: %s
                - Status: %s
                - Total Records: %d
                - Successful: %d
                - Failed: %d
                - Success Rate: %s
                - Dry Run: %s
                - Failure Reason: %s
                - Duration: %s to %s

                Please summarize this migration result.
                """,
                job.getJobName(),
                job.getSourceObject(),
                job.getTargetObject(),
                job.getStatus(),
                job.getTotalRecords(),
                job.getSuccessCount(),
                job.getFailureCount(),
                successRate,
                job.isDryRun() ? "Yes (no data written)" : "No (real migration)",
                job.getFailureReason() != null ?
                        job.getFailureReason() : "None",
                job.getStartedAt(),
                job.getCompletedAt());

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userMessage)
            ));

            String summary = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText();

            log.info("AI summary generated for job: {}", job.getJobId());
            return summary;

        } catch (Exception e) {
            log.error("AI summarization failed: {}", e.getMessage());
            return String.format(
                    "Migration %s: %d/%d records processed. " +
                    "%d failures. Status: %s",
                    job.getJobId(),
                    job.getSuccessCount(),
                    job.getTotalRecords(),
                    job.getFailureCount(),
                    job.getStatus());
        }
    }

    public String recommendPreMigration(String sourceObject,
                                         int totalRecords,
                                         List<String> failedChecks) {

        log.info("Generating pre-migration recommendations for {}",
                sourceObject);

        String systemPrompt = """
                You are a Salesforce migration expert.
                Review the pre-flight validation results and give
                specific recommendations before the migration runs.
                Be concise. Maximum 150 words.
                Use bullet points.
                """;

        String userMessage = String.format("""
                Pre-migration validation for: %s
                Total records to migrate: %d
                Failed checks: %s

                What should the user do before running migration?
                """,
                sourceObject,
                totalRecords,
                failedChecks.isEmpty() ?
                        "None - all checks passed" :
                        String.join(", ", failedChecks));

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userMessage)
            ));

            return chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText();

        } catch (Exception e) {
            log.error("Pre-migration recommendation failed: {}",
                    e.getMessage());
            return "AI recommendations unavailable. " +
                   "Review validation report manually.";
        }
    }
}

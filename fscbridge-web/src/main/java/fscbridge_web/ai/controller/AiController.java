package fscbridge_web.ai.controller;

import fscbridge_core.model.MigrationJob;
import fscbridge_web.ai.service.FieldMappingSuggester;
import fscbridge_web.ai.service.MigrationSummarizer;
import fscbridge_web.ai.service.NaturalLanguageParser;
import fscbridge_web.service.MigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final FieldMappingSuggester fieldMappingSuggester;
    private final MigrationSummarizer migrationSummarizer;
    private final NaturalLanguageParser naturalLanguageParser;
    private final MigrationService migrationService;

    @PostMapping("/suggest-mapping")
    public ResponseEntity<Map<String, String>> suggestMapping(
            @RequestBody Map<String, Object> request) {

        String fieldName = (String) request.get("fieldName");
        String objectContext = (String) request.get("objectContext");

        @SuppressWarnings("unchecked")
        List<String> sampleValues = (List<String>)
                request.getOrDefault("sampleValues", List.of());

        log.info("AI mapping suggestion requested for: {}", fieldName);

        String suggestion = fieldMappingSuggester.suggestMapping(
                fieldName, sampleValues, objectContext);

        return ResponseEntity.ok(Map.of(
                "field", fieldName,
                "suggestion", suggestion
        ));
    }

    @PostMapping("/suggest-bulk-mappings")
    public ResponseEntity<Map<String, String>> suggestBulkMappings(
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        List<String> fields = (List<String>) request.get("fields");
        String objectContext = (String) request.get("objectContext");

        String suggestions = fieldMappingSuggester
                .suggestBulkMappings(fields, objectContext);

        return ResponseEntity.ok(Map.of(
                "objectContext", objectContext,
                "suggestions", suggestions
        ));
    }

    @PostMapping("/migrate")
    public ResponseEntity<?> naturalLanguageMigrate(
            @RequestBody Map<String, String> request) {

        String naturalLanguage = request.get("request");
        log.info("Natural language migration: {}", naturalLanguage);

        MigrationJob job = naturalLanguageParser
                .parseToMigrationJob(naturalLanguage);

        if (job == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "PARSE_FAILED",
                    "message", "Could not understand your request. " +
                               "Try: 'migrate financial accounts as dry run'"
            ));
        }

        MigrationJob completedJob = migrationService.runMigration(job);

        String summary = migrationSummarizer.summarizeJob(completedJob);

        return ResponseEntity.ok(Map.of(
                "parsedJob", completedJob,
                "aiSummary", summary
        ));
    }

    @PostMapping("/summarize")
    public ResponseEntity<Map<String, String>> summarizeJob(
            @RequestBody MigrationJob job) {

        log.info("AI summary requested for job: {}", job.getJobId());

        String summary = migrationSummarizer.summarizeJob(job);

        return ResponseEntity.ok(Map.of(
                "jobId", job.getJobId(),
                "summary", summary
        ));
    }

    @PostMapping("/recommend")
    public ResponseEntity<Map<String, String>> recommend(
            @RequestBody Map<String, Object> request) {

        String sourceObject = (String) request.get("sourceObject");
        int totalRecords = (Integer) request.getOrDefault(
                "totalRecords", 0);

        @SuppressWarnings("unchecked")
        List<String> failedChecks = (List<String>)
                request.getOrDefault("failedChecks", List.of());

        String recommendation = migrationSummarizer
                .recommendPreMigration(
                        sourceObject, totalRecords, failedChecks);

        return ResponseEntity.ok(Map.of(
                "sourceObject", sourceObject,
                "recommendation", recommendation
        ));
    }
}

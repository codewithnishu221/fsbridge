package fscbridge_web.controller;

import fscbridge_web.validator.PreFlightValidator;
import fscbridge_web.validator.ValidationReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final PreFlightValidator preFlightValidator;


    @PostMapping("/run")
    public ResponseEntity<ValidationReport> runValidation(
            @RequestBody Map<String, String> request) {

        String sourceObject = request.get("sourceObject");
        String targetObject = request.get("targetObject");

        log.info("Running pre-flight validation: {} → {}",
                sourceObject, targetObject);

        ValidationReport report = preFlightValidator.validate(
                sourceObject, targetObject);

        return ResponseEntity.ok(report);
    }


    @GetMapping("/connection-test")
    public ResponseEntity<Map<String, Object>> connectionTest() {
        log.info("Running connection test");

        try {
            ValidationReport report = preFlightValidator.validate(
                    "Account", "Account");

            boolean connected = report.getChecks().stream()
                    .anyMatch(c -> c.getCheckName()
                            .equals("Source Org Connection") &&
                            c.getStatus() ==
                                    fscbridge_web.validator.ValidationResult
                                            .ValidationStatus.PASS);

            return ResponseEntity.ok(Map.of(
                    "connected", connected,
                    "message", connected ?
                            "Salesforce connection successful" :
                            "Salesforce connection failed"
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "connected", false,
                    "message", e.getMessage()
            ));
        }
    }
}
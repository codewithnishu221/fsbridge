package fscbridge_web.validator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReport {


    private ValidationResult.ValidationStatus overallStatus;

    private String sourceObject;
    private String targetObject;
    private int totalRecordsFound;
    private int estimatedApiCalls;
    private List<ValidationResult> checks;
    private long passCount;
    private long warnCount;
    private long failCount;


    private LocalDateTime validatedAt;


    public boolean isSafeToMigrate() {
        return overallStatus != ValidationResult.ValidationStatus.FAIL;
    }
}
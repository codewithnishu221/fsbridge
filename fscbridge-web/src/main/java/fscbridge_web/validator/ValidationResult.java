package fscbridge_web.validator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {


    private String checkName;


    private ValidationStatus status;


    private String message;


    private String detail;


    public static ValidationResult pass(String checkName, String message) {
        return ValidationResult.builder()
                .checkName(checkName)
                .status(ValidationStatus.PASS)
                .message(message)
                .build();
    }

    public static ValidationResult fail(String checkName,
                                        String message,
                                        String detail) {
        return ValidationResult.builder()
                .checkName(checkName)
                .status(ValidationStatus.FAIL)
                .message(message)
                .detail(detail)
                .build();
    }

    public static ValidationResult warn(String checkName,
                                        String message,
                                        String detail) {
        return ValidationResult.builder()
                .checkName(checkName)
                .status(ValidationStatus.WARN)
                .message(message)
                .detail(detail)
                .build();
    }


    public enum ValidationStatus {
        PASS,
        WARN,
        FAIL
    }
}
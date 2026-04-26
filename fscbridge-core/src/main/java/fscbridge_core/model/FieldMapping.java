package fscbridge_core.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMapping {

    private String sourceField;
    private String targetField;
    private String dataType;
    private boolean required;
    private String defaultValue;
    private boolean skip;
}

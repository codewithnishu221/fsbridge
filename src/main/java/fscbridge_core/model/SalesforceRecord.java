package fscbridge_core.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesforceRecord {

    private String id;
    private String objectType;
    private Map<String, Object> fields;
//    private String errorMessage;
//    private String tragetId;
    @Builder.Default
    private Boolean migrated = false;
    @Builder.Default
    private String errorMessage = "";
    @Builder.Default
    private String targetId ="";
}

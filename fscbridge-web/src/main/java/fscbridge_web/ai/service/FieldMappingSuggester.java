package fscbridge_web.ai.service;

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
public class FieldMappingSuggester {

    private final ChatModel chatModel;

    public String suggestMapping(String sourceFieldName,
                                  List<String> sampleValues,
                                  String objectContext) {

        log.info("Requesting AI mapping suggestion for field: {}",
                sourceFieldName);

        String systemPrompt = """
                You are a Salesforce FSC (Financial Services Cloud) expert.
                Your job is to suggest the best FSC Core standard field mapping
                for a given source field from an old FSC managed package.

                FSC Core standard objects include: FinancialAccount, FinancialGoal,
                AccountGroup (Household), FinancialHolding, AssetsAndLiabilities.

                Respond in this exact format:
                SUGGESTED_TARGET_FIELD: <field API name>
                CONFIDENCE: <HIGH/MEDIUM/LOW>
                DATA_TYPE: <STRING/DECIMAL/DATE/BOOLEAN/LOOKUP>
                REASON: <one sentence explanation>
                ALTERNATIVE: <alternative field if any, or NONE>
                """;

        String userMessage = String.format("""
                Source field name: %s
                Object context: %s
                Sample values: %s

                What FSC Core standard field does this map to?
                """,
                sourceFieldName,
                objectContext,
                String.join(", ", sampleValues));

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userMessage)
            ));

            String response = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText();

            log.info("AI suggestion received for field: {}", sourceFieldName);
            return response;

        } catch (Exception e) {
            log.error("AI field mapping failed for {}: {}",
                    sourceFieldName, e.getMessage());
            return "AI_UNAVAILABLE: Could not get suggestion. " +
                   "Error: " + e.getMessage();
        }
    }

    public String suggestBulkMappings(List<String> unknownFields,
                                       String objectContext) {

        log.info("Requesting bulk AI mapping for {} fields in {}",
                unknownFields.size(), objectContext);

        String systemPrompt = """
                You are a Salesforce FSC expert.
                Suggest FSC Core field mappings for a list of source fields.
                For each field respond with one line:
                FIELD_NAME -> TARGET_FIELD | CONFIDENCE | DATA_TYPE
                If no mapping exists, write: FIELD_NAME -> SKIP | LOW | N/A
                """;

        String fieldList = String.join("\n", unknownFields);
        String userMessage = String.format("""
                Object: %s
                Fields needing mapping:
                %s

                Suggest FSC Core mappings for each field.
                """, objectContext, fieldList);

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
            log.error("Bulk AI mapping failed: {}", e.getMessage());
            return "AI_UNAVAILABLE: " + e.getMessage();
        }
    }
}

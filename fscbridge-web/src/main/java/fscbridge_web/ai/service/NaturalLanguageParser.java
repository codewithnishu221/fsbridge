package fscbridge_web.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class NaturalLanguageParser {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public MigrationJob parseToMigrationJob(
            String naturalLanguageRequest) {

        log.info("Parsing natural language request: {}",
                naturalLanguageRequest);

        String systemPrompt = """
                You are a Salesforce FSC migration assistant.
                Convert natural language migration requests to JSON.

                Salesforce FSC object mappings:
                - "financial accounts" / "accounts" ->
                  sourceObject: "FinServ__FinancialAccount__c",
                  targetObject: "FinancialAccount"
                - "households" / "household" ->
                  sourceObject: "FinServ__Household__c",
                  targetObject: "AccountGroup"
                - "financial goals" / "goals" ->
                  sourceObject: "FinServ__FinancialGoal__c",
                  targetObject: "FinancialGoal"
                - "contacts" ->
                  sourceObject: "Contact",
                  targetObject: "Contact"

                If user says "test" or "dry run" or "check"
                set dryRun: true. Otherwise dryRun: false.

                Respond ONLY with valid JSON, nothing else:
                {
                  "jobName": "descriptive name",
                  "sourceObject": "API name",
                  "targetObject": "API name",
                  "dryRun": true/false
                }
                """;

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(naturalLanguageRequest)
            ));

            String jsonResponse = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText()
                    .trim();

            jsonResponse = jsonResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            log.debug("AI parsed JSON: {}", jsonResponse);

            MigrationJob job = objectMapper.readValue(
                    jsonResponse, MigrationJob.class);

            log.info("Successfully parsed: {} -> {}",
                    job.getSourceObject(), job.getTargetObject());

            return job;

        } catch (Exception e) {
            log.error("Natural language parsing failed: {}",
                    e.getMessage());
            return null;
        }
    }
}

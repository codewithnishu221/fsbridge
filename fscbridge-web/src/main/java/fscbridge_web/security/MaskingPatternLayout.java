package fscbridge_web.security;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskingPatternLayout extends PatternLayout {

    private static final List<Pattern> MASKING_PATTERNS = new ArrayList<>();

    static {
        MASKING_PATTERNS.add(Pattern.compile(
                "(Bearer\\s)[A-Za-z0-9\\-_\\.]+",
                Pattern.CASE_INSENSITIVE
        ));

        MASKING_PATTERNS.add(Pattern.compile(
                "(client_secret=)[^&\\s\"']+",
                Pattern.CASE_INSENSITIVE
        ));

        MASKING_PATTERNS.add(Pattern.compile(
                "(client_id=)[^&\\s\"']+",
                Pattern.CASE_INSENSITIVE
        ));

        MASKING_PATTERNS.add(Pattern.compile(
                "(access_token=)[^&\\s\"',}]+",
                Pattern.CASE_INSENSITIVE
        ));

        MASKING_PATTERNS.add(Pattern.compile(
                "(password[\"']?\\s*[:=]\\s*[\"']?)[^&\\s\"',}]+",
                Pattern.CASE_INSENSITIVE
        ));

        MASKING_PATTERNS.add(Pattern.compile(
                "(clientSecret[\"']?\\s*[:=]\\s*[\"']?)[^&\\s\"',}]+",
                Pattern.CASE_INSENSITIVE
        ));

        MASKING_PATTERNS.add(Pattern.compile(
                "(clientId[\"']?\\s*[:=]\\s*[\"']?)[^&\\s\"',}]+",
                Pattern.CASE_INSENSITIVE
        ));

        MASKING_PATTERNS.add(Pattern.compile(
                "(Authorization[\"']?\\s*[:=]\\s*[\"']?Bearer\\s)[^\\s\"',}]+",
                Pattern.CASE_INSENSITIVE
        ));

        MASKING_PATTERNS.add(Pattern.compile(
                "(/services/oauth2/token\\?)[^\\s\"']+",
                Pattern.CASE_INSENSITIVE
        ));
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        String formattedMessage = super.doLayout(event);
        return maskSensitiveData(formattedMessage);
    }

    private String maskSensitiveData(String message) {
        if (message == null) return null;

        String masked = message;

        for (Pattern pattern : MASKING_PATTERNS) {
            Matcher matcher = pattern.matcher(masked);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String replacement = matcher.group(1) + "***MASKED***";
                matcher.appendReplacement(result,
                        Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(result);
            masked = result.toString();
        }

        return masked;
    }

    public static List<Pattern> getMaskingPatterns() {
        return MASKING_PATTERNS;
    }
}

package com.crevolab.testseries.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuestionParserService {

    // Matches question start patterns like "1.", "1)", "Q1.", "Q.1", "Q 1.", "Question 1:", etc.
    private static final Pattern QUESTION_START_PATTERN = Pattern.compile(
            "(?i)^\\s*(?:(?:q(?:uestion)?\\s*\\.\\s*(\\d+)(?:[\\.\\)\\:\\-]\\s*)?)|(?:q(?:uestion)?\\s*(\\d+)\\s*[\\.\\)\\:\\-]?\\s*)|(?:(\\d+)\\s*[\\.\\)\\:\\-](?!\\d)\\s*))(.*)$"
    );

    // Matches option start at line beginning like "A.", "A)", "(A)", "[A]", "a.", "(a)", etc.
    private static final Pattern OPTION_START_PATTERN = Pattern.compile(
            "(?i)^\\s*(?:\\(([A-D])\\)|\\[([A-D])\\]|([A-D])\\s*[\\.\\)\\:\\-])\\s*(.*)$"
    );

    // Matches inline option delimiters like "  (B)", "  B.", "  (C)", etc. on the same line
    private static final Pattern INLINE_OPTION_PATTERN = Pattern.compile(
            "(?i)(?:\\s{2,}|\\s+)(?:\\(([B-D])\\)|\\[([B-D])\\]|([B-D])\\s*[\\.\\)\\:\\-])\\s*"
    );

    // Headers/footers like "Page 2 of 5", "- 2 -", "- Page 2 -", "Page 2", "[Page 2]"
    private static final Pattern HEADER_FOOTER_PATTERN = Pattern.compile(
            "(?i)^\\s*(?:[-=~_*#\\[\\(]*\\s*page\\s+\\d+(?:\\s*(?:of|\\/)\\s*\\d+)?\\s*[-=~_*#\\]\\)]*|[-=~_*#]+\\s*\\d+\\s*[-=~_*#]+|[-=~_*#]+\\s*page\\s+\\d+\\s*[-=~_*#]+|\\[?\\s*page\\s+\\d+\\s*\\]?|\\d+\\s*(?:of|\\/)\\s*\\d+)\\s*$"
    );

    // Strips option prefix like "(B)", "B.", "[B]", "B)"
    private static final Pattern OPTION_PREFIX_PATTERN = Pattern.compile(
            "(?i)^\\s*(?:\\([A-D]\\)|\\[[A-D]\\]|[A-D]\\s*[\\.\\)\\:\\-])\\s*"
    );

    public record ParsedQuestion(
            int orderNo,
            String questionNumberStr,
            String text,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            Map<String, String> options
    ) {
        public String getText() { return text; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public int getOrderNo() { return orderNo; }
        public String getQuestionNumberStr() { return questionNumberStr; }
    }

    public record ParseResult(
            List<ParsedQuestion> questions,
            List<String> warnings
    ) {
        public List<ParsedQuestion> getQuestions() { return questions; }
        public List<String> getWarnings() { return warnings; }
    }

    private static class QuestionBuilder {
        int orderNo;
        String questionNumberStr;
        StringBuilder textBuilder = new StringBuilder();
        Map<String, StringBuilder> options = new LinkedHashMap<>();
        String currentOptionKey = null;

        QuestionBuilder(int orderNo, String questionNumberStr, String initialText) {
            this.orderNo = orderNo;
            this.questionNumberStr = questionNumberStr;
            if (initialText != null && !initialText.isBlank()) {
                textBuilder.append(initialText.trim());
            }
        }

        void appendText(String line) {
            if (line == null || line.isBlank()) return;
            if (textBuilder.length() > 0) {
                textBuilder.append("\n");
            }
            textBuilder.append(line.trim());
        }

        void setOption(String key, String initialText) {
            currentOptionKey = key.toUpperCase();
            StringBuilder sb = options.computeIfAbsent(currentOptionKey, k -> new StringBuilder());
            if (initialText != null && !initialText.isBlank()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(initialText.trim());
            }
        }

        void appendOption(String line) {
            if (currentOptionKey != null && line != null && !line.isBlank()) {
                StringBuilder sb = options.computeIfAbsent(currentOptionKey, k -> new StringBuilder());
                if (sb.length() > 0) sb.append(" ");
                sb.append(line.trim());
            }
        }

        ParsedQuestion build() {
            String optA = options.containsKey("A") ? options.get("A").toString().trim() : "";
            String optB = options.containsKey("B") ? options.get("B").toString().trim() : "";
            String optC = options.containsKey("C") ? options.get("C").toString().trim() : "";
            String optD = options.containsKey("D") ? options.get("D").toString().trim() : "";

            Map<String, String> finalOptions = new LinkedHashMap<>();
            finalOptions.put("A", optA);
            finalOptions.put("B", optB);
            finalOptions.put("C", optC);
            finalOptions.put("D", optD);

            return new ParsedQuestion(
                    orderNo,
                    questionNumberStr,
                    textBuilder.toString().trim(),
                    optA,
                    optB,
                    optC,
                    optD,
                    finalOptions
            );
        }
    }

    public ParseResult parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ParseResult(Collections.emptyList(), Collections.emptyList());
        }

        List<QuestionBuilder> builders = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        String[] lines = rawText.split("\\r?\\n");
        QuestionBuilder currentBuilder = null;
        int orderCounter = 1;

        for (String rawLine : lines) {
            String trimmedLine = rawLine.trim();

            if (trimmedLine.isEmpty()) {
                continue;
            }

            // Ignore headers/footers like "Page 2 of 5"
            if (isHeaderOrFooter(trimmedLine)) {
                continue;
            }

            // 1. Check if line starts a new Question
            Matcher qMatcher = QUESTION_START_PATTERN.matcher(rawLine);
            if (qMatcher.matches()) {
                String qNumStr = qMatcher.group(1);
                if (qNumStr == null) qNumStr = qMatcher.group(2);
                if (qNumStr == null) qNumStr = qMatcher.group(3);

                String initialQuestionText = qMatcher.group(4);

                currentBuilder = new QuestionBuilder(orderCounter++, qNumStr, initialQuestionText);
                builders.add(currentBuilder);
                continue;
            }

            // If we have not encountered any question yet, skip stray intro lines
            if (currentBuilder == null) {
                continue;
            }

            // 2. Check if line starts an Option (A-D)
            Matcher optMatcher = OPTION_START_PATTERN.matcher(rawLine);
            if (optMatcher.matches()) {
                String optKey = optMatcher.group(1);
                if (optKey == null) optKey = optMatcher.group(2);
                if (optKey == null) optKey = optMatcher.group(3);
                optKey = optKey.toUpperCase();

                String optTextRemainder = optMatcher.group(4);

                // Check for multiple options on the same line (e.g. "(A) Apple  (B) Banana  (C) Cherry  (D) Date")
                parseOptionsOnLine(currentBuilder, optKey, optTextRemainder);
                continue;
            }

            // 3. Continuation line: append to current option or question text
            if (currentBuilder.currentOptionKey != null) {
                currentBuilder.appendOption(trimmedLine);
            } else {
                currentBuilder.appendText(trimmedLine);
            }
        }

        // Build questions and generate warnings
        List<ParsedQuestion> resultQuestions = new ArrayList<>();
        for (QuestionBuilder b : builders) {
            ParsedQuestion pq = b.build();
            resultQuestions.add(pq);

            String qIdentifier = pq.questionNumberStr() != null ? pq.questionNumberStr() : String.valueOf(pq.orderNo());

            // Check no question text
            if (pq.text().isBlank()) {
                warnings.add("Question " + qIdentifier + " has no question text");
            }

            // Check fewer than 4 options
            int foundOptions = 0;
            if (!pq.optionA().isBlank()) foundOptions++;
            if (!pq.optionB().isBlank()) foundOptions++;
            if (!pq.optionC().isBlank()) foundOptions++;
            if (!pq.optionD().isBlank()) foundOptions++;

            if (foundOptions < 4) {
                warnings.add("Question " + qIdentifier + " has fewer than 4 options (found " + foundOptions + ")");
            }
        }

        return new ParseResult(resultQuestions, warnings);
    }

    private void parseOptionsOnLine(QuestionBuilder builder, String firstOptKey, String remainder) {
        if (remainder == null || remainder.isBlank()) {
            builder.setOption(firstOptKey, "");
            return;
        }

        // Check if there are other inline options (e.g. B, C, D) in remainder
        Matcher inlineMatcher = INLINE_OPTION_PATTERN.matcher(remainder);
        List<Integer> splitIndices = new ArrayList<>();
        List<String> optionKeys = new ArrayList<>();

        splitIndices.add(0);
        optionKeys.add(firstOptKey);

        while (inlineMatcher.find()) {
            String key = inlineMatcher.group(1);
            if (key == null) key = inlineMatcher.group(2);
            if (key == null) key = inlineMatcher.group(3);
            if (key != null) {
                splitIndices.add(inlineMatcher.start());
                optionKeys.add(key.toUpperCase());
            }
        }

        if (optionKeys.size() == 1) {
            builder.setOption(firstOptKey, remainder.trim());
        } else {
            // Slice segments
            for (int i = 0; i < optionKeys.size(); i++) {
                String currentKey = optionKeys.get(i);
                int start = splitIndices.get(i);
                int end = (i + 1 < optionKeys.size()) ? splitIndices.get(i + 1) : remainder.length();
                String segment = remainder.substring(start, end).trim();

                // If not first option, strip the leading marker that matched INLINE_OPTION_PATTERN
                if (i > 0) {
                    segment = OPTION_PREFIX_PATTERN.matcher(segment).replaceFirst("");
                }
                builder.setOption(currentKey, segment.trim());
            }
        }
    }

    public boolean isHeaderOrFooter(String line) {
        if (line == null || line.isBlank()) return false;
        String trimmed = line.trim();
        if (HEADER_FOOTER_PATTERN.matcher(trimmed).matches()) {
            return true;
        }
        return trimmed.matches("(?i)^.*page\\s+\\d+\\s*(?:of|\\/)\\s*\\d+.*$");
    }
}

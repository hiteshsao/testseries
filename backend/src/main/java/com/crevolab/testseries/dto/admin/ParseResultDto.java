package com.crevolab.testseries.dto.admin;

import java.util.ArrayList;
import java.util.List;

public class ParseResultDto {

    private int questionsCreated;
    private List<String> warnings = new ArrayList<>();

    public ParseResultDto() {
    }

    public ParseResultDto(int questionsCreated, List<String> warnings) {
        this.questionsCreated = questionsCreated;
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public int getQuestionsCreated() {
        return questionsCreated;
    }

    public void setQuestionsCreated(int questionsCreated) {
        this.questionsCreated = questionsCreated;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }
}

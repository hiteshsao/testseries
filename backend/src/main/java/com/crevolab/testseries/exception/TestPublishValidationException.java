package com.crevolab.testseries.exception;

import java.util.ArrayList;
import java.util.List;

public class TestPublishValidationException extends RuntimeException {

    private final List<String> problems;

    public TestPublishValidationException(List<String> problems) {
        super("Test cannot be published: " + (problems != null ? String.join("; ", problems) : "validation errors"));
        this.problems = problems != null ? new ArrayList<>(problems) : new ArrayList<>();
    }

    public List<String> getProblems() {
        return problems;
    }
}

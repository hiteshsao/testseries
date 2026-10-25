package com.crevolab.testseries.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PdfRawTextResponseDto {

    private Long testId;
    private String sourcePdfPath;
    private String rawText;
    private Integer pageCount;
    private Integer characterCount;

    public PdfRawTextResponseDto() {
    }

    public PdfRawTextResponseDto(Long testId, String sourcePdfPath, String rawText, Integer pageCount, Integer characterCount) {
        this.testId = testId;
        this.sourcePdfPath = sourcePdfPath;
        this.rawText = rawText;
        this.pageCount = pageCount;
        this.characterCount = characterCount;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getSourcePdfPath() {
        return sourcePdfPath;
    }

    public void setSourcePdfPath(String sourcePdfPath) {
        this.sourcePdfPath = sourcePdfPath;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getText() {
        return rawText;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(Integer characterCount) {
        this.characterCount = characterCount;
    }
}

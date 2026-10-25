package com.crevolab.testseries.exception;

public class ScannedPdfException extends RuntimeException {

    public static final String ERROR_CODE = "SCANNED_PDF_NOT_SUPPORTED";
    public static final String DEFAULT_MESSAGE = "This looks like a scanned PDF. OCR is not supported in this prototype. Please upload a text-based PDF.";

    public ScannedPdfException() {
        super(DEFAULT_MESSAGE);
    }

    public ScannedPdfException(String message) {
        super(message != null ? message : DEFAULT_MESSAGE);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}

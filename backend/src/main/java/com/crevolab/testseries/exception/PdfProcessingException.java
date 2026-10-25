package com.crevolab.testseries.exception;

public class PdfProcessingException extends RuntimeException {

    private final String errorCode;

    public PdfProcessingException(String message) {
        super(message);
        this.errorCode = "PDF_PROCESSING_ERROR";
    }

    public PdfProcessingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PdfProcessingException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

package com.crevolab.testseries.service;

import com.crevolab.testseries.exception.PdfProcessingException;
import com.crevolab.testseries.exception.ScannedPdfException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfTextExtractionService {

    private static final Logger log = LoggerFactory.getLogger(PdfTextExtractionService.class);
    public static final double MIN_AVG_CHARS_PER_PAGE = 40.0;

    public record ExtractionResult(String text, int pageCount, int characterCount, double averageCharsPerPage) {}

    public String extractText(File file) {
        return extractTextWithMetadata(file).text();
    }

    public String extractText(Path path) {
        return extractTextWithMetadata(path).text();
    }

    public String extractText(byte[] bytes) {
        return extractTextWithMetadata(bytes).text();
    }

    public ExtractionResult extractTextWithMetadata(Path path) {
        if (path == null) {
            throw new PdfProcessingException("PDF file path cannot be null", "PDF_INVALID_PATH");
        }
        if (!Files.exists(path)) {
            throw new PdfProcessingException("PDF file not found at: " + path, "PDF_NOT_FOUND");
        }
        return extractTextWithMetadata(path.toFile());
    }

    public ExtractionResult extractTextWithMetadata(File file) {
        if (file == null) {
            throw new PdfProcessingException("PDF file cannot be null", "PDF_INVALID_FILE");
        }
        if (!file.exists()) {
            throw new PdfProcessingException("PDF file not found: " + file.getPath(), "PDF_NOT_FOUND");
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            return processDocument(document);
        } catch (InvalidPasswordException e) {
            log.warn("Attempted to load password-protected PDF: {}", file.getName());
            throw new PdfProcessingException("PDF is password protected or encrypted. Please upload an unencrypted PDF.", "PDF_ENCRYPTED");
        } catch (IOException e) {
            log.warn("Corrupt or invalid PDF file: {} - {}", file.getName(), e.getMessage());
            throw new PdfProcessingException("PDF file is corrupt or invalid. Please upload a valid PDF document.", "PDF_CORRUPT");
        } catch (ScannedPdfException | PdfProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during PDF text extraction: {}", e.getMessage(), e);
            throw new PdfProcessingException("Failed to extract text from PDF: " + e.getMessage(), "PDF_PROCESSING_ERROR");
        }
    }

    public ExtractionResult extractTextWithMetadata(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new PdfProcessingException("PDF content bytes cannot be empty", "PDF_INVALID_FILE");
        }

        try (PDDocument document = Loader.loadPDF(bytes)) {
            return processDocument(document);
        } catch (InvalidPasswordException e) {
            throw new PdfProcessingException("PDF is password protected or encrypted. Please upload an unencrypted PDF.", "PDF_ENCRYPTED");
        } catch (IOException e) {
            throw new PdfProcessingException("PDF file is corrupt or invalid. Please upload a valid PDF document.", "PDF_CORRUPT");
        } catch (ScannedPdfException | PdfProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfProcessingException("Failed to extract text from PDF: " + e.getMessage(), "PDF_PROCESSING_ERROR");
        }
    }

    private ExtractionResult processDocument(PDDocument document) throws IOException {
        if (document.isEncrypted()) {
            throw new PdfProcessingException("PDF is password protected or encrypted. Please upload an unencrypted PDF.", "PDF_ENCRYPTED");
        }

        int numberOfPages = document.getNumberOfPages();
        if (numberOfPages <= 0) {
            throw new PdfProcessingException("PDF document contains zero pages.", "PDF_ZERO_PAGES");
        }

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        String text = stripper.getText(document);

        String trimmed = text != null ? text.trim() : "";
        int totalChars = trimmed.length();
        double avgCharsPerPage = (double) totalChars / numberOfPages;

        if (trimmed.isEmpty() || avgCharsPerPage < MIN_AVG_CHARS_PER_PAGE) {
            throw new ScannedPdfException("This looks like a scanned PDF. OCR is not supported in this prototype. Please upload a text-based PDF.");
        }

        return new ExtractionResult(text, numberOfPages, totalChars, avgCharsPerPage);
    }
}

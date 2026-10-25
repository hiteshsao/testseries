package com.crevolab.testseries;

import com.crevolab.testseries.exception.PdfProcessingException;
import com.crevolab.testseries.exception.ScannedPdfException;
import com.crevolab.testseries.service.PdfTextExtractionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfTextExtractionServiceTest {

    private PdfTextExtractionService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new PdfTextExtractionService();
    }

    private byte[] createPdfWithPagesAndText(String... pageTexts) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (String text : pageTexts) {
                PDPage page = new PDPage();
                document.addPage(page);
                if (text != null && !text.isEmpty()) {
                    try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                        stream.beginText();
                        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                        stream.newLineAtOffset(50, 700);
                        stream.showText(text);
                        stream.endText();
                    }
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createEncryptedPdf() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("This is secret text that cannot be accessed without password.");
                stream.endText();
            }

            AccessPermission ap = new AccessPermission();
            StandardProtectionPolicy spp = new StandardProtectionPolicy("ownerSecret", "userSecret", ap);
            spp.setEncryptionKeyLength(128);
            document.protect(spp);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    @Test
    @DisplayName("1. Successfully extracts text from a standard text-based PDF")
    void testExtractTextSuccess() throws Exception {
        String content = "Question 1: What is the primary difference between a process and a thread in OS?";
        byte[] pdfBytes = createPdfWithPagesAndText(content);

        File pdfFile = tempDir.resolve("valid.pdf").toFile();
        Files.write(pdfFile.toPath(), pdfBytes);

        String extracted = service.extractText(pdfFile);

        assertThat(extracted).isNotNull();
        assertThat(extracted).contains("Question 1:");
        assertThat(extracted).contains("primary difference between a process and a thread");

        PdfTextExtractionService.ExtractionResult result = service.extractTextWithMetadata(pdfFile);
        assertThat(result.pageCount()).isEqualTo(1);
        assertThat(result.characterCount()).isGreaterThanOrEqualTo(40);
        assertThat(result.averageCharsPerPage()).isGreaterThanOrEqualTo(40.0);
    }

    @Test
    @DisplayName("2. Detects scanned/empty PDF (0 chars) and throws SCANNED_PDF_NOT_SUPPORTED")
    void testEmptyScannedPdfThrowsScannedPdfException() throws Exception {
        // 1 page with no text at all (blank page / image-only simulation)
        byte[] pdfBytes = createPdfWithPagesAndText("");

        File pdfFile = tempDir.resolve("blank.pdf").toFile();
        Files.write(pdfFile.toPath(), pdfBytes);

        assertThatThrownBy(() -> service.extractText(pdfFile))
                .isInstanceOf(ScannedPdfException.class)
                .hasMessage("This looks like a scanned PDF. OCR is not supported in this prototype. Please upload a text-based PDF.")
                .satisfies(ex -> {
                    ScannedPdfException scannedEx = (ScannedPdfException) ex;
                    assertThat(scannedEx.getErrorCode()).isEqualTo("SCANNED_PDF_NOT_SUPPORTED");
                });
    }

    @Test
    @DisplayName("3. Detects PDF with average characters under 40 per page and throws SCANNED_PDF_NOT_SUPPORTED")
    void testLowCharCountThrowsScannedPdfException() throws Exception {
        // 1 page with only 12 characters (< 40)
        byte[] pdfBytes = createPdfWithPagesAndText("Short text!");

        File pdfFile = tempDir.resolve("short.pdf").toFile();
        Files.write(pdfFile.toPath(), pdfBytes);

        assertThatThrownBy(() -> service.extractText(pdfFile))
                .isInstanceOf(ScannedPdfException.class)
                .hasMessage("This looks like a scanned PDF. OCR is not supported in this prototype. Please upload a text-based PDF.")
                .satisfies(ex -> {
                    ScannedPdfException scannedEx = (ScannedPdfException) ex;
                    assertThat(scannedEx.getErrorCode()).isEqualTo("SCANNED_PDF_NOT_SUPPORTED");
                });
    }

    @Test
    @DisplayName("4. Multipage PDF with average under 40 chars/page throws SCANNED_PDF_NOT_SUPPORTED")
    void testMultipageLowAverageThrowsScannedPdfException() throws Exception {
        // 2 pages with 25 characters each (total 50 chars / 2 pages = 25 avg < 40)
        byte[] pdfBytes = createPdfWithPagesAndText("Page 1 header text.", "Page 2 footer text.");

        File pdfFile = tempDir.resolve("multipage_low.pdf").toFile();
        Files.write(pdfFile.toPath(), pdfBytes);

        assertThatThrownBy(() -> service.extractText(pdfFile))
                .isInstanceOf(ScannedPdfException.class)
                .hasMessage("This looks like a scanned PDF. OCR is not supported in this prototype. Please upload a text-based PDF.");
    }

    @Test
    @DisplayName("5. Multipage PDF with average >= 40 chars/page extracts text successfully")
    void testMultipageSufficientTextSucceeds() throws Exception {
        String page1 = "Question 1: Explain the difference between TCP and UDP protocols in networking.";
        String page2 = "Question 2: What is the time complexity of QuickSort in the worst case scenario?";
        byte[] pdfBytes = createPdfWithPagesAndText(page1, page2);

        File pdfFile = tempDir.resolve("multipage_ok.pdf").toFile();
        Files.write(pdfFile.toPath(), pdfBytes);

        PdfTextExtractionService.ExtractionResult result = service.extractTextWithMetadata(pdfFile);

        assertThat(result.pageCount()).isEqualTo(2);
        assertThat(result.text()).contains("TCP and UDP");
        assertThat(result.text()).contains("QuickSort");
        assertThat(result.averageCharsPerPage()).isGreaterThanOrEqualTo(40.0);
    }

    @Test
    @DisplayName("6. Gracefully handles encrypted PDFs without throwing unexpected exceptions")
    void testEncryptedPdfThrowsPdfProcessingException() throws Exception {
        byte[] encryptedBytes = createEncryptedPdf();
        File pdfFile = tempDir.resolve("encrypted.pdf").toFile();
        Files.write(pdfFile.toPath(), encryptedBytes);

        assertThatThrownBy(() -> service.extractText(pdfFile))
                .isInstanceOf(PdfProcessingException.class)
                .satisfies(ex -> {
                    PdfProcessingException pex = (PdfProcessingException) ex;
                    assertThat(pex.getErrorCode()).isEqualTo("PDF_ENCRYPTED");
                    assertThat(pex.getMessage()).contains("encrypted");
                });
    }

    @Test
    @DisplayName("7. Gracefully handles corrupt PDF files with clear error message")
    void testCorruptPdfThrowsPdfProcessingException() throws Exception {
        byte[] corruptBytes = "%PDF-1.5 CORRUPT_RANDOM_GARBAGE_NOT_A_VALID_PDF_STRUCTURE".getBytes();
        File pdfFile = tempDir.resolve("corrupt.pdf").toFile();
        Files.write(pdfFile.toPath(), corruptBytes);

        assertThatThrownBy(() -> service.extractText(pdfFile))
                .isInstanceOf(PdfProcessingException.class)
                .satisfies(ex -> {
                    PdfProcessingException pex = (PdfProcessingException) ex;
                    assertThat(pex.getErrorCode()).isEqualTo("PDF_CORRUPT");
                    assertThat(pex.getMessage()).contains("corrupt");
                });
    }

    @Test
    @DisplayName("8. Text extraction uses sorted position ordering")
    void testSortedByPositionTextExtraction() throws Exception {
        // Create a PDF with two lines added in reverse visual order
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                // First draw the BOTTOM line (y = 500)
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 500);
                stream.showText("Line 2: This is at the bottom of the page in terms of Y coordinate.");
                stream.endText();

                // Then draw the TOP line (y = 700)
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Line 1: This is at the top of the page in terms of Y coordinate.");
                stream.endText();
            }

            File pdfFile = tempDir.resolve("sorted.pdf").toFile();
            document.save(pdfFile);

            String extracted = service.extractText(pdfFile);

            // Because sortByPosition is true, Line 1 (top) must appear before Line 2 (bottom)
            int indexLine1 = extracted.indexOf("Line 1");
            int indexLine2 = extracted.indexOf("Line 2");

            assertThat(indexLine1).isGreaterThanOrEqualTo(0);
            assertThat(indexLine2).isGreaterThanOrEqualTo(0);
            assertThat(indexLine1).isLessThan(indexLine2);
        }
    }
}

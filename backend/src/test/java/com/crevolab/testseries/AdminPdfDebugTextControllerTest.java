package com.crevolab.testseries;

import com.crevolab.testseries.controller.admin.AdminTestSeriesController;
import com.crevolab.testseries.dto.admin.PdfRawTextResponseDto;
import com.crevolab.testseries.exception.PdfProcessingException;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.exception.ScannedPdfException;
import com.crevolab.testseries.service.AdminTestSeriesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTestSeriesController.class)
class AdminPdfDebugTextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminTestSeriesService adminTestSeriesService;

    @Test
    @DisplayName("GET /api/admin/tests/{id}/raw-text returns 200 with rawText JSON")
    void testGetRawExtractedTextReturnsOk() throws Exception {
        Long testId = 1L;
        String sampleText = "Q1. What is the complexity of binary search?\nA. O(1)\nB. O(log n)\nC. O(n)\nD. O(n log n)";
        PdfRawTextResponseDto dto = new PdfRawTextResponseDto(testId, "uploads/abc.pdf", sampleText, 1, sampleText.length());

        given(adminTestSeriesService.getRawExtractedText(testId)).willReturn(dto);

        mockMvc.perform(get("/api/admin/tests/{id}/raw-text", testId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testId").value(testId))
                .andExpect(jsonPath("$.sourcePdfPath").value("uploads/abc.pdf"))
                .andExpect(jsonPath("$.rawText", containsString("binary search")))
                .andExpect(jsonPath("$.text", containsString("binary search")))
                .andExpect(jsonPath("$.pageCount").value(1));
    }

    @Test
    @DisplayName("GET /api/admin/tests/{id}/raw-text with text/plain returns 200 with plain text string")
    void testGetRawExtractedTextPlainReturnsOk() throws Exception {
        Long testId = 1L;
        String sampleText = "Q1. Explain virtual memory.\nDetailed explanation follows...";
        PdfRawTextResponseDto dto = new PdfRawTextResponseDto(testId, "uploads/abc.pdf", sampleText, 1, sampleText.length());

        given(adminTestSeriesService.getRawExtractedText(testId)).willReturn(dto);

        mockMvc.perform(get("/api/admin/tests/{id}/raw-text", testId)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Explain virtual memory.")));
    }

    @Test
    @DisplayName("GET /api/admin/tests/{id}/raw-text on scanned PDF returns 400 with SCANNED_PDF_NOT_SUPPORTED")
    void testGetRawExtractedTextScannedPdfReturns400() throws Exception {
        Long testId = 1L;

        given(adminTestSeriesService.getRawExtractedText(testId))
                .willThrow(new ScannedPdfException());

        mockMvc.perform(get("/api/admin/tests/{id}/raw-text", testId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("SCANNED_PDF_NOT_SUPPORTED"))
                .andExpect(jsonPath("$.errorCode").value("SCANNED_PDF_NOT_SUPPORTED"))
                .andExpect(jsonPath("$.message").value(
                        "This looks like a scanned PDF. OCR is not supported in this prototype. Please upload a text-based PDF."
                ));
    }

    @Test
    @DisplayName("GET /api/admin/tests/{id}/raw-text on encrypted PDF returns 400 with PDF_ENCRYPTED")
    void testGetRawExtractedTextEncryptedPdfReturns400() throws Exception {
        Long testId = 1L;

        given(adminTestSeriesService.getRawExtractedText(testId))
                .willThrow(new PdfProcessingException("PDF is password protected or encrypted. Please upload an unencrypted PDF.", "PDF_ENCRYPTED"));

        mockMvc.perform(get("/api/admin/tests/{id}/raw-text", testId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("PDF_ENCRYPTED"))
                .andExpect(jsonPath("$.message", containsString("encrypted")));
    }

    @Test
    @DisplayName("GET /api/admin/tests/{id}/raw-text on corrupt PDF returns 400 with PDF_CORRUPT")
    void testGetRawExtractedTextCorruptPdfReturns400() throws Exception {
        Long testId = 1L;

        given(adminTestSeriesService.getRawExtractedText(testId))
                .willThrow(new PdfProcessingException("PDF file is corrupt or invalid. Please upload a valid PDF document.", "PDF_CORRUPT"));

        mockMvc.perform(get("/api/admin/tests/{id}/raw-text", testId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("PDF_CORRUPT"))
                .andExpect(jsonPath("$.message", containsString("corrupt")));
    }

    @Test
    @DisplayName("GET /api/admin/tests/{id}/raw-text when test not found returns 404")
    void testGetRawExtractedTextTestNotFoundReturns404() throws Exception {
        Long nonExistentId = 999L;

        given(adminTestSeriesService.getRawExtractedText(nonExistentId))
                .willThrow(new ResourceNotFoundException("Test not found with id: 999"));

        mockMvc.perform(get("/api/admin/tests/{id}/raw-text", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Test not found with id: 999"));
    }

    @Test
    @DisplayName("POST /api/admin/tests/debug/extract-text extracts text directly from uploaded PDF")
    void testDirectDebugExtractionReturnsOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "%PDF-1.4 sample content with enough text to be extracted".getBytes()
        );

        PdfRawTextResponseDto dto = new PdfRawTextResponseDto(null, "test.pdf", "Sample extracted text content", 1, 30);
        given(adminTestSeriesService.debugExtractText(any())).willReturn(dto);

        mockMvc.perform(multipart("/api/admin/tests/debug/extract-text")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawText").value("Sample extracted text content"));
    }
}

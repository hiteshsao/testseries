package com.crevolab.testseries;

import com.crevolab.testseries.controller.admin.AdminTestSeriesController;
import com.crevolab.testseries.dto.admin.TestResponseDto;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.service.AdminTestSeriesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTestSeriesController.class)
class AdminPdfUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminTestSeriesService adminTestSeriesService;

    @Test
    @DisplayName("POST /api/admin/tests/{id}/upload-pdf with valid PDF returns 200 and updated test DTO")
    void testUploadPdfReturnsOk() throws Exception {
        Long testId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "questions.pdf",
                "application/pdf",
                "%PDF-1.4 sample content".getBytes()
        );

        TestResponseDto responseDto = new TestResponseDto();
        responseDto.setId(testId);
        responseDto.setTitle("Sample Test");
        responseDto.setSourcePdfPath("uploads/123e4567-e89b-12d3-a456-426614174000.pdf");

        given(adminTestSeriesService.uploadPdf(eq(testId), any())).willReturn(responseDto);

        mockMvc.perform(multipart("/api/admin/tests/{id}/upload-pdf", testId)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId))
                .andExpect(jsonPath("$.sourcePdfPath").value("uploads/123e4567-e89b-12d3-a456-426614174000.pdf"));
    }

    @Test
    @DisplayName("POST /api/admin/tests/{id}/upload-pdf with invalid file returns 400 Bad Request with ApiErrorResponse")
    void testUploadPdfReturnsBadRequestOnValidationError() throws Exception {
        Long testId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "not-a-pdf.txt",
                "text/plain",
                "hello world".getBytes()
        );

        given(adminTestSeriesService.uploadPdf(eq(testId), any()))
                .willThrow(new IllegalArgumentException("Invalid file extension. Only .pdf files are allowed"));

        mockMvc.perform(multipart("/api/admin/tests/{id}/upload-pdf", testId)
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid file extension. Only .pdf files are allowed"));
    }

    @Test
    @DisplayName("POST /api/admin/tests/{id}/upload-pdf with missing test ID returns 404 Not Found")
    void testUploadPdfReturnsNotFoundWhenTestMissing() throws Exception {
        Long testId = 9999L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "paper.pdf",
                "application/pdf",
                "%PDF-1.4 valid".getBytes()
        );

        given(adminTestSeriesService.uploadPdf(eq(testId), any()))
                .willThrow(new ResourceNotFoundException("Test not found with id: 9999"));

        mockMvc.perform(multipart("/api/admin/tests/{id}/upload-pdf", testId)
                        .file(file))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Test not found with id: 9999"));
    }
}

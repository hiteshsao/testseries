package com.crevolab.testseries;

import com.crevolab.testseries.controller.admin.AdminTestSeriesController;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.TestStatus;
import com.crevolab.testseries.dto.admin.TestResponseDto;
import com.crevolab.testseries.exception.TestPublishValidationException;
import com.crevolab.testseries.service.AdminTestSeriesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTestSeriesController.class)
class AdminTestSeriesPublishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminTestSeriesService adminTestSeriesService;

    @Test
    @DisplayName("POST /api/admin/tests/{id}/publish returns 400 with problems list when validation fails")
    void testPostPublishFailsWithProblems() throws Exception {
        Long testId = 42L;
        List<String> problems = List.of(
                "Test duration must be greater than 0 minutes",
                "Paid test must have a price greater than 0.00",
                "Test must have at least one question to be published",
                "Question 1 has no correct answer"
        );

        given(adminTestSeriesService.publishTest(testId))
                .willThrow(new TestPublishValidationException(problems));

        mockMvc.perform(post("/api/admin/tests/{id}/publish", testId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.problems", hasSize(4)))
                .andExpect(jsonPath("$.problems", hasItem("Test duration must be greater than 0 minutes")))
                .andExpect(jsonPath("$.problems", hasItem("Paid test must have a price greater than 0.00")))
                .andExpect(jsonPath("$.problems", hasItem("Test must have at least one question to be published")))
                .andExpect(jsonPath("$.problems", hasItem("Question 1 has no correct answer")));
    }

    private TestResponseDto createResponseDto(Long id, TestStatus status) {
        TestResponseDto dto = new TestResponseDto();
        dto.setId(id);
        dto.setTitle("Mock Test 1");
        dto.setInstructions("Instructions");
        dto.setDurationMinutes(60);
        dto.setStatus(status);
        dto.setAccessType(AccessType.FREE);
        dto.setPrice(BigDecimal.ZERO);
        dto.setMaxAttempts(1);
        dto.setMaxSolutionViews(3);
        dto.setMaxPdfDownloads(1);
        dto.setCreatedAt(Instant.now());
        return dto;
    }

    @Test
    @DisplayName("POST /api/admin/tests/{id}/publish returns 200 when test is valid")
    void testPostPublishSucceeds() throws Exception {
        Long testId = 42L;
        TestResponseDto responseDto = createResponseDto(testId, TestStatus.PUBLISHED);

        given(adminTestSeriesService.publishTest(testId)).willReturn(responseDto);

        mockMvc.perform(post("/api/admin/tests/{id}/publish", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("POST /api/admin/tests/{id}/unpublish returns 200 and DRAFT status")
    void testPostUnpublishSucceeds() throws Exception {
        Long testId = 42L;
        TestResponseDto responseDto = createResponseDto(testId, TestStatus.DRAFT);

        given(adminTestSeriesService.unpublishTest(testId)).willReturn(responseDto);

        mockMvc.perform(post("/api/admin/tests/{id}/unpublish", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("PATCH /api/admin/tests/{id}/publish and unpublish also supported")
    void testPatchPublishAndUnpublish() throws Exception {
        Long testId = 42L;
        TestResponseDto pub = createResponseDto(testId, TestStatus.PUBLISHED);
        TestResponseDto unpub = createResponseDto(testId, TestStatus.DRAFT);

        given(adminTestSeriesService.publishTest(testId)).willReturn(pub);
        given(adminTestSeriesService.unpublishTest(testId)).willReturn(unpub);

        mockMvc.perform(patch("/api/admin/tests/{id}/publish", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(patch("/api/admin/tests/{id}/unpublish", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }
}

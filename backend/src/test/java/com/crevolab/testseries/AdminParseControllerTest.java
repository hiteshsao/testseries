package com.crevolab.testseries;

import com.crevolab.testseries.controller.admin.AdminTestSeriesController;
import com.crevolab.testseries.dto.admin.ParseResultDto;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.service.AdminTestSeriesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTestSeriesController.class)
class AdminParseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminTestSeriesService adminTestSeriesService;

    @Test
    @DisplayName("POST /api/admin/tests/{id}/parse with valid rawText returns 200 with questionsCreated and warnings")
    void testParseQuestionsReturnsOk() throws Exception {
        Long testId = 1L;
        ParseResultDto resultDto = new ParseResultDto(5, List.of("Question 3 has fewer than 4 options (found 3)"));

        given(adminTestSeriesService.parseAndSaveQuestions(eq(testId), any())).willReturn(resultDto);

        String jsonPayload = """
                {
                    "rawText": "1. Sample question\\nA. Opt1\\nB. Opt2\\nC. Opt3\\nD. Opt4"
                }
                """;

        mockMvc.perform(post("/api/admin/tests/{id}/parse", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionsCreated").value(5))
                .andExpect(jsonPath("$.warnings[0]").value("Question 3 has fewer than 4 options (found 3)"));
    }

    @Test
    @DisplayName("POST /api/admin/tests/{id}/parse without body (trigger parse from uploaded PDF) returns 200")
    void testParseQuestionsWithoutBodyReturnsOk() throws Exception {
        Long testId = 1L;
        ParseResultDto resultDto = new ParseResultDto(10, List.of());

        given(adminTestSeriesService.parseAndSaveQuestions(eq(testId), eq(null))).willReturn(resultDto);

        mockMvc.perform(post("/api/admin/tests/{id}/parse", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionsCreated").value(10))
                .andExpect(jsonPath("$.warnings").isArray())
                .andExpect(jsonPath("$.warnings").isEmpty());
    }

    @Test
    @DisplayName("POST /api/admin/tests/{id}/parse when test does not exist returns 404")
    void testParseQuestionsNotFound() throws Exception {
        Long testId = 999L;
        given(adminTestSeriesService.parseAndSaveQuestions(eq(testId), any()))
                .willThrow(new ResourceNotFoundException("Test not found with id: " + testId));

        mockMvc.perform(post("/api/admin/tests/{id}/parse", testId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Test not found with id: 999")));
    }

    @Test
    @DisplayName("POST /api/admin/tests/{id}/parse when no PDF uploaded returns 400 Bad Request")
    void testParseQuestionsNoPdfBadRequest() throws Exception {
        Long testId = 2L;
        given(adminTestSeriesService.parseAndSaveQuestions(eq(testId), eq(null)))
                .willThrow(new IllegalArgumentException("No PDF uploaded for test id: " + testId + ". Upload a PDF or provide rawText."));

        mockMvc.perform(post("/api/admin/tests/{id}/parse", testId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("No PDF uploaded for test id: 2")));
    }
}

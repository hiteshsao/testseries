package com.crevolab.testseries;

import com.crevolab.testseries.controller.admin.AdminQuestionController;
import com.crevolab.testseries.dto.admin.QuestionRequestDto;
import com.crevolab.testseries.dto.admin.QuestionResponseDto;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.service.AdminQuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminQuestionController.class)
class AdminQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminQuestionService adminQuestionService;

    @Test
    @DisplayName("GET /api/admin/tests/{id}/questions returns 200 and list of questions")
    void testGetQuestionsByTestId() throws Exception {
        Long testId = 1L;
        QuestionResponseDto q1 = new QuestionResponseDto(
                10L, testId, 1, "Question 1 text", "OptA", "OptB", "OptC", "OptD",
                "A", BigDecimal.valueOf(4.00), BigDecimal.valueOf(1.00)
        );
        QuestionResponseDto q2 = new QuestionResponseDto(
                11L, testId, 2, "Question 2 text", "OptA", "OptB", "OptC", "OptD",
                null, BigDecimal.valueOf(4.00), BigDecimal.valueOf(1.00)
        );

        given(adminQuestionService.getQuestionsByTestId(testId)).willReturn(List.of(q1, q2));

        mockMvc.perform(get("/api/admin/tests/{id}/questions", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].orderNo").value(1))
                .andExpect(jsonPath("$[0].correctOption").value("A"))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].correctOption").isEmpty());
    }

    @Test
    @DisplayName("GET /api/admin/tests/{id}/questions returns 404 when test not found")
    void testGetQuestionsByTestIdNotFound() throws Exception {
        Long testId = 999L;
        given(adminQuestionService.getQuestionsByTestId(testId))
                .willThrow(new ResourceNotFoundException("Test not found with id: " + testId));

        mockMvc.perform(get("/api/admin/tests/{id}/questions", testId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Test not found with id: 999")));
    }

    @Test
    @DisplayName("POST /api/admin/tests/{id}/questions with valid payload returns 201 Created")
    void testCreateQuestionValid() throws Exception {
        Long testId = 1L;
        QuestionRequestDto request = new QuestionRequestDto(
                "What is the speed of light?",
                "3x10^8 m/s", "3x10^6 m/s", "3x10^5 m/s", "3x10^3 m/s",
                "A",
                BigDecimal.valueOf(4.00),
                BigDecimal.valueOf(1.00)
        );

        QuestionResponseDto created = new QuestionResponseDto(
                50L, testId, 1, request.getText(),
                request.getOptionA(), request.getOptionB(), request.getOptionC(), request.getOptionD(),
                "A", request.getMarks(), request.getNegativeMarks()
        );

        given(adminQuestionService.createQuestion(eq(testId), any(QuestionRequestDto.class))).willReturn(created);

        mockMvc.perform(post("/api/admin/tests/{id}/questions", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.orderNo").value(1))
                .andExpect(jsonPath("$.text").value("What is the speed of light?"))
                .andExpect(jsonPath("$.correctOption").value("A"));
    }

    @Test
    @DisplayName("POST /api/admin/tests/{id}/questions allows null correct_option")
    void testCreateQuestionWithNullCorrectOption() throws Exception {
        Long testId = 1L;
        QuestionRequestDto request = new QuestionRequestDto(
                "Sample question?",
                "Opt A", "Opt B", "Opt C", "Opt D",
                null,
                BigDecimal.valueOf(4.00),
                BigDecimal.valueOf(0.00) // negative marks = 0 allowed
        );

        QuestionResponseDto created = new QuestionResponseDto(
                51L, testId, 2, request.getText(),
                request.getOptionA(), request.getOptionB(), request.getOptionC(), request.getOptionD(),
                null, request.getMarks(), request.getNegativeMarks()
        );

        given(adminQuestionService.createQuestion(eq(testId), any(QuestionRequestDto.class))).willReturn(created);

        mockMvc.perform(post("/api/admin/tests/{id}/questions", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correctOption").isEmpty());
    }

    @Test
    @DisplayName("Validation: text required -> 400 Bad Request")
    void testValidationTextRequired() throws Exception {
        QuestionRequestDto request = new QuestionRequestDto(
                "", // Blank text
                "A", "B", "C", "D",
                "A",
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(1)
        );

        mockMvc.perform(post("/api/admin/tests/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'text')]").exists());
    }

    @Test
    @DisplayName("Validation: options A-D required -> 400 Bad Request")
    void testValidationOptionsRequired() throws Exception {
        // Missing optionB and optionD
        QuestionRequestDto request = new QuestionRequestDto(
                "Valid text",
                "Option A", "", "Option C", "  ",
                "A",
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(1)
        );

        mockMvc.perform(post("/api/admin/tests/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'optionB')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'optionD')]").exists());
    }

    @Test
    @DisplayName("Validation: correct_option not in A-D -> 400 Bad Request")
    void testValidationInvalidCorrectOption() throws Exception {
        QuestionRequestDto request = new QuestionRequestDto(
                "Valid question",
                "A", "B", "C", "D",
                "E", // Invalid correct option
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(1)
        );

        mockMvc.perform(post("/api/admin/tests/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'correctOption')]").exists());
    }

    @Test
    @DisplayName("Validation: marks > 0 (marks=0 or negative fails) -> 400 Bad Request")
    void testValidationMarksMustBeGreaterThanZero() throws Exception {
        QuestionRequestDto request = new QuestionRequestDto(
                "Valid question",
                "A", "B", "C", "D",
                "B",
                BigDecimal.valueOf(0.00), // Zero marks
                BigDecimal.valueOf(1.00)
        );

        mockMvc.perform(post("/api/admin/tests/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'marks')]").exists());
    }

    @Test
    @DisplayName("Validation: negative_marks >= 0 (negative_marks < 0 fails) -> 400 Bad Request")
    void testValidationNegativeMarksMustBePositiveOrZero() throws Exception {
        QuestionRequestDto request = new QuestionRequestDto(
                "Valid question",
                "A", "B", "C", "D",
                "B",
                BigDecimal.valueOf(4.00),
                BigDecimal.valueOf(-1.00) // Negative
        );

        mockMvc.perform(post("/api/admin/tests/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'negativeMarks')]").exists());
    }

    @Test
    @DisplayName("PUT /api/admin/questions/{id} with valid payload returns 200 OK")
    void testUpdateQuestionValid() throws Exception {
        Long questionId = 20L;
        QuestionRequestDto request = new QuestionRequestDto(
                "Updated question prompt",
                "Updated A", "Updated B", "Updated C", "Updated D",
                "C",
                BigDecimal.valueOf(4.00),
                BigDecimal.valueOf(1.00)
        );

        QuestionResponseDto updated = new QuestionResponseDto(
                questionId, 1L, 2, request.getText(),
                request.getOptionA(), request.getOptionB(), request.getOptionC(), request.getOptionD(),
                "C", request.getMarks(), request.getNegativeMarks()
        );

        given(adminQuestionService.updateQuestion(eq(questionId), any(QuestionRequestDto.class))).willReturn(updated);

        mockMvc.perform(put("/api/admin/questions/{id}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questionId))
                .andExpect(jsonPath("$.text").value("Updated question prompt"))
                .andExpect(jsonPath("$.correctOption").value("C"));
    }

    @Test
    @DisplayName("PUT /api/admin/questions/{id} returns 404 when question not found")
    void testUpdateQuestionNotFound() throws Exception {
        Long questionId = 999L;
        QuestionRequestDto request = new QuestionRequestDto(
                "Valid text", "A", "B", "C", "D", "A", BigDecimal.valueOf(4), BigDecimal.valueOf(1)
        );

        given(adminQuestionService.updateQuestion(eq(questionId), any(QuestionRequestDto.class)))
                .willThrow(new ResourceNotFoundException("Question not found with id: " + questionId));

        mockMvc.perform(put("/api/admin/questions/{id}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Question not found with id: 999")));
    }

    @Test
    @DisplayName("DELETE /api/admin/questions/{id} returns 204 No Content")
    void testDeleteQuestion() throws Exception {
        Long questionId = 20L;
        doNothing().when(adminQuestionService).deleteQuestion(questionId);

        mockMvc.perform(delete("/api/admin/questions/{id}", questionId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/admin/questions/{id} returns 404 when question not found")
    void testDeleteQuestionNotFound() throws Exception {
        Long questionId = 999L;
        doThrow(new ResourceNotFoundException("Question not found with id: " + questionId))
                .when(adminQuestionService).deleteQuestion(questionId);

        mockMvc.perform(delete("/api/admin/questions/{id}", questionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Question not found with id: 999")));
    }
}

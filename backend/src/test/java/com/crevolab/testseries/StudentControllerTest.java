package com.crevolab.testseries;

import com.crevolab.testseries.controller.student.StudentController;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.AttemptStatus;
import com.crevolab.testseries.domain.enums.TestStatus;
import com.crevolab.testseries.dto.student.StudentAnswerDto;
import com.crevolab.testseries.dto.student.StudentAnswerRequestDto;
import com.crevolab.testseries.dto.student.StudentAnswerResponseDto;
import com.crevolab.testseries.dto.student.StudentAttemptResponseDto;
import com.crevolab.testseries.dto.student.StudentQuestionDto;
import com.crevolab.testseries.dto.student.StudentTestSummaryDto;
import com.crevolab.testseries.service.StudentTestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentTestService studentTestService;

    @Test
    @DisplayName("GET /api/student/tests returns 200 with PUBLISHED tests only and demo header")
    void testGetPublishedTests() throws Exception {
        StudentTestSummaryDto summary = new StudentTestSummaryDto(
                101L, "Mathematics Mock Exam", "Do not cheat", 180,
                TestStatus.PUBLISHED, AccessType.FREE, BigDecimal.ZERO,
                2, 0L, true, true, 30, BigDecimal.valueOf(120), Instant.now()
        );

        given(studentTestService.getPublishedTests("student-demo-42")).willReturn(List.of(summary));

        mockMvc.perform(get("/api/student/tests")
                        .header("X-Demo-Student-Id", "student-demo-42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].title").value("Mathematics Mock Exam"));
    }

    @Test
    @DisplayName("POST /api/student/tests/{id}/start returns questions WITHOUT correct_option (Security Assertion)")
    void testStartTestSecurityNoCorrectOptionInJson() throws Exception {
        Long testId = 101L;
        String studentId = "student-demo-99";

        StudentQuestionDto q1 = new StudentQuestionDto(
                1L, 1, "What is Newton's First Law?", "Inertia", "F=ma", "Action Reaction", "Gravity",
                BigDecimal.valueOf(4.0), BigDecimal.valueOf(1.0)
        );
        StudentQuestionDto q2 = new StudentQuestionDto(
                2L, 2, "What is the unit of force?", "Joule", "Newton", "Watt", "Pascal",
                BigDecimal.valueOf(4.0), BigDecimal.valueOf(1.0)
        );

        Instant now = Instant.now();
        Instant endsAt = now.plusSeconds(3600);

        StudentAttemptResponseDto attemptDto = new StudentAttemptResponseDto(
                555L, testId, "Physics Mock", studentId, now, endsAt, 3600L, 60,
                AttemptStatus.IN_PROGRESS, BigDecimal.valueOf(8.0), List.of(q1, q2), List.of()
        );

        given(studentTestService.startTest(eq(testId), eq(studentId))).willReturn(attemptDto);

        MvcResult result = mockMvc.perform(post("/api/student/tests/{id}/start", testId)
                        .header("X-Demo-Student-Id", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(555))
                .andExpect(jsonPath("$.remainingSeconds").value(3600))
                .andExpect(jsonPath("$.questions", hasSize(2)))
                .andExpect(jsonPath("$.questions[0].marks").value(4.0))
                .andExpect(jsonPath("$.questions[0].negativeMarks").value(1.0))
                // Exact jsonPath assertions ensuring correct_option / correctOption does NOT exist
                .andExpect(jsonPath("$.questions[0].correct_option").doesNotExist())
                .andExpect(jsonPath("$.questions[0].correctOption").doesNotExist())
                .andExpect(jsonPath("$.questions[1].correct_option").doesNotExist())
                .andExpect(jsonPath("$.questions[1].correctOption").doesNotExist())
                .andExpect(jsonPath("$..correct_option").doesNotExist())
                .andExpect(jsonPath("$..correctOption").doesNotExist())
                .andReturn();

        // Complete raw JSON body string assertion
        String jsonContent = result.getResponse().getContentAsString();
        assertThat(jsonContent.toLowerCase()).doesNotContain("correct_option");
        assertThat(jsonContent.toLowerCase()).doesNotContain("correctoption");
    }

    @Test
    @DisplayName("POST /api/student/tests/{id}/start fails with 400 when paid test is not purchased")
    void testStartTestFailsWhenUnpurchasedPaid() throws Exception {
        Long testId = 202L;
        String studentId = "student-free-user";

        given(studentTestService.startTest(eq(testId), eq(studentId)))
                .willThrow(new IllegalArgumentException("This is a paid test (₹499.00) and must be purchased before attempting."));

        mockMvc.perform(post("/api/student/tests/{id}/start", testId)
                        .header("X-Demo-Student-Id", studentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("paid test")));
    }

    @Test
    @DisplayName("PUT /api/student/attempts/{id}/answer saves answer successfully")
    void testSaveAnswerSucceeds() throws Exception {
        Long attemptId = 555L;
        String studentId = "student-demo-99";
        StudentAnswerRequestDto req = new StudentAnswerRequestDto(1L, "A", true);
        StudentAnswerResponseDto res = new StudentAnswerResponseDto(1L, "A", true, 3450L);

        given(studentTestService.saveAnswer(eq(attemptId), eq(studentId), any())).willReturn(res);

        mockMvc.perform(put("/api/student/attempts/{id}/answer", attemptId)
                        .header("X-Demo-Student-Id", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.selectedOption").value("A"))
                .andExpect(jsonPath("$.markedForReview").value(true))
                .andExpect(jsonPath("$.remainingSeconds").value(3450));
    }

    @Test
    @DisplayName("PUT /api/student/attempts/{id}/answer fails with 400 when ends_at has passed")
    void testSaveAnswerFailsWhenExpired() throws Exception {
        Long attemptId = 555L;
        String studentId = "student-demo-99";
        StudentAnswerRequestDto req = new StudentAnswerRequestDto(1L, "A", false);

        given(studentTestService.saveAnswer(eq(attemptId), eq(studentId), any()))
                .willThrow(new IllegalArgumentException("Attempt time limit has expired."));

        mockMvc.perform(put("/api/student/attempts/{id}/answer", attemptId)
                        .header("X-Demo-Student-Id", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("expired")));
    }

    @Test
    @DisplayName("PUT /api/student/attempts/{id}/answer fails with 400 when attempt was submitted")
    void testSaveAnswerFailsWhenSubmitted() throws Exception {
        Long attemptId = 555L;
        String studentId = "student-demo-99";
        StudentAnswerRequestDto req = new StudentAnswerRequestDto(1L, "A", false);

        given(studentTestService.saveAnswer(eq(attemptId), eq(studentId), any()))
                .willThrow(new IllegalArgumentException("Attempt has already been submitted and cannot be modified."));

        mockMvc.perform(put("/api/student/attempts/{id}/answer", attemptId)
                        .header("X-Demo-Student-Id", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("already been submitted")));
    }

    @Test
    @DisplayName("GET /api/student/attempts/{id} resumes attempt with answers + remaining seconds without correct_option in JSON")
    void testResumeAttemptResumesWithAnswersAndNoCorrectOptionInJson() throws Exception {
        Long attemptId = 555L;
        String studentId = "student-demo-99";

        StudentQuestionDto q1 = new StudentQuestionDto(
                1L, 1, "What is Newton's First Law?", "Inertia", "F=ma", "Action Reaction", "Gravity",
                BigDecimal.valueOf(4.0), BigDecimal.valueOf(1.0)
        );
        StudentAnswerDto ans1 = new StudentAnswerDto(1L, "A", true);

        Instant now = Instant.now();
        Instant endsAt = now.plusSeconds(1800);

        StudentAttemptResponseDto attemptDto = new StudentAttemptResponseDto(
                attemptId, 101L, "Physics Mock", studentId, now.minusSeconds(1800), endsAt, 1800L, 60,
                AttemptStatus.IN_PROGRESS, BigDecimal.valueOf(4.0), List.of(q1), List.of(ans1)
        );

        given(studentTestService.getAttempt(eq(attemptId), eq(studentId))).willReturn(attemptDto);

        MvcResult result = mockMvc.perform(get("/api/student/attempts/{id}", attemptId)
                        .header("X-Demo-Student-Id", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(attemptId))
                .andExpect(jsonPath("$.remainingSeconds").value(1800))
                .andExpect(jsonPath("$.answers", hasSize(1)))
                .andExpect(jsonPath("$.answers[0].questionId").value(1))
                .andExpect(jsonPath("$.answers[0].selectedOption").value("A"))
                .andExpect(jsonPath("$.answers[0].markedForReview").value(true))
                .andExpect(jsonPath("$.questions", hasSize(1)))
                // Verify correct_option is NEVER present
                .andExpect(jsonPath("$..correct_option").doesNotExist())
                .andExpect(jsonPath("$..correctOption").doesNotExist())
                .andReturn();

        String jsonContent = result.getResponse().getContentAsString();
        assertThat(jsonContent.toLowerCase()).doesNotContain("correct_option");
        assertThat(jsonContent.toLowerCase()).doesNotContain("correctoption");
    }
}

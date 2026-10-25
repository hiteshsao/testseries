package com.crevolab.testseries;

import com.crevolab.testseries.controller.admin.AdminTestSeriesController;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.service.AdminTestSeriesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTestSeriesController.class)
class AdminTestSeriesValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminTestSeriesService adminTestSeriesService;

    @Test
    @DisplayName("1. Title required validation: empty title returns 400 with field 'title'")
    void testBlankTitleReturnsBadRequest() throws Exception {
        String payload = """
            {
                "title": "",
                "durationMinutes": 60,
                "accessType": "FREE",
                "maxAttempts": 1,
                "maxSolutionViews": 3,
                "maxPdfDownloads": 1
            }
            """;

        mockMvc.perform(post("/api/admin/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem(allOf(
                        hasEntry("field", "title"),
                        hasEntry("message", "Title is required")
                ))));
    }

    @Test
    @DisplayName("2. Duration >= 1 validation: duration 0 returns 400 with field 'durationMinutes'")
    void testInvalidDurationReturnsBadRequest() throws Exception {
        String payload = """
            {
                "title": "Mathematics Mock Test",
                "durationMinutes": 0,
                "accessType": "FREE",
                "maxAttempts": 1,
                "maxSolutionViews": 3,
                "maxPdfDownloads": 1
            }
            """;

        mockMvc.perform(post("/api/admin/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem(allOf(
                        hasEntry("field", "durationMinutes"),
                        hasEntry("message", "Duration must be at least 1 minute")
                ))));
    }

    @Test
    @DisplayName("3. Max attempts >= 1 validation: maxAttempts 0 returns 400 with field 'maxAttempts'")
    void testInvalidMaxAttemptsReturnsBadRequest() throws Exception {
        String payload = """
            {
                "title": "Science Quiz",
                "durationMinutes": 45,
                "accessType": "FREE",
                "maxAttempts": 0,
                "maxSolutionViews": 3,
                "maxPdfDownloads": 1
            }
            """;

        mockMvc.perform(post("/api/admin/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem(allOf(
                        hasEntry("field", "maxAttempts"),
                        hasEntry("message", "Max attempts must be at least 1")
                ))));
    }

    @Test
    @DisplayName("4. PAID requires price > 0 validation: PAID test with 0 price returns 400 with field 'price'")
    void testPaidTestWithZeroPriceReturnsBadRequest() throws Exception {
        String payload = """
            {
                "title": "Premium GATE Test",
                "durationMinutes": 180,
                "accessType": "PAID",
                "price": 0.00,
                "maxAttempts": 2,
                "maxSolutionViews": 5,
                "maxPdfDownloads": 2
            }
            """;

        mockMvc.perform(post("/api/admin/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem(allOf(
                        hasEntry("field", "price"),
                        hasEntry("message", "Price must be greater than 0 for PAID tests")
                ))));
    }
}

package com.crevolab.testseries;

import com.crevolab.testseries.controller.admin.AdminStatsController;
import com.crevolab.testseries.dto.admin.AdminStatsDto;
import com.crevolab.testseries.service.AdminTestSeriesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminStatsController.class)
class AdminStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminTestSeriesService adminTestSeriesService;

    @Test
    @DisplayName("GET /api/admin/stats returns 200 with dashboard statistics")
    void testGetStatsReturnsOk() throws Exception {
        given(adminTestSeriesService.getAdminStats())
                .willReturn(new AdminStatsDto(10, 7, 3, 42));

        mockMvc.perform(get("/api/admin/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTests").value(10))
                .andExpect(jsonPath("$.publishedTests").value(7))
                .andExpect(jsonPath("$.draftTests").value(3))
                .andExpect(jsonPath("$.totalAttempts").value(42));
    }
}

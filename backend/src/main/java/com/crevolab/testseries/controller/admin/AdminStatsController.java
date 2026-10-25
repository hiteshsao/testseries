package com.crevolab.testseries.controller.admin;

import com.crevolab.testseries.dto.admin.AdminStatsDto;
import com.crevolab.testseries.service.AdminTestSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final AdminTestSeriesService adminTestSeriesService;

    public AdminStatsController(AdminTestSeriesService adminTestSeriesService) {
        this.adminTestSeriesService = adminTestSeriesService;
    }

    @GetMapping
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminTestSeriesService.getAdminStats());
    }
}

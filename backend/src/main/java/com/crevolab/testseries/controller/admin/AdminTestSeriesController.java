package com.crevolab.testseries.controller.admin;

import com.crevolab.testseries.dto.admin.PdfRawTextResponseDto;
import com.crevolab.testseries.dto.admin.TestRequestDto;
import com.crevolab.testseries.dto.admin.TestResponseDto;
import com.crevolab.testseries.service.AdminTestSeriesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tests")
public class AdminTestSeriesController {

    private final AdminTestSeriesService adminTestSeriesService;

    public AdminTestSeriesController(AdminTestSeriesService adminTestSeriesService) {
        this.adminTestSeriesService = adminTestSeriesService;
    }

    @PostMapping
    public ResponseEntity<TestResponseDto> createTest(@Valid @RequestBody TestRequestDto request) {
        TestResponseDto created = adminTestSeriesService.createTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TestResponseDto>> getAllTests() {
        return ResponseEntity.ok(adminTestSeriesService.getAllTests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResponseDto> getTestById(@PathVariable Long id) {
        return ResponseEntity.ok(adminTestSeriesService.getTestById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestResponseDto> updateTest(
            @PathVariable Long id,
            @Valid @RequestBody TestRequestDto request) {
        return ResponseEntity.ok(adminTestSeriesService.updateTest(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long id) {
        adminTestSeriesService.deleteTest(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<TestResponseDto> publishTest(@PathVariable Long id) {
        return ResponseEntity.ok(adminTestSeriesService.publishTest(id));
    }

    @PostMapping(value = "/{id}/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TestResponseDto> uploadPdf(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(adminTestSeriesService.uploadPdf(id, file));
    }

    // DEMO-ONLY: Temporary admin debug endpoint returning raw extracted text for a test's uploaded PDF
    @GetMapping(value = {"/{id}/raw-text", "/{id}/extract-text"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PdfRawTextResponseDto> getRawExtractedText(@PathVariable Long id) {
        return ResponseEntity.ok(adminTestSeriesService.getRawExtractedText(id));
    }

    // DEMO-ONLY: Temporary admin debug endpoint returning plain text
    @GetMapping(value = {"/{id}/raw-text", "/{id}/extract-text"}, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getRawExtractedTextPlain(@PathVariable Long id) {
        return ResponseEntity.ok(adminTestSeriesService.getRawExtractedText(id).getRawText());
    }

    // DEMO-ONLY: Temporary admin debug endpoint returning raw extracted text directly from an uploaded PDF
    @PostMapping(value = "/debug/extract-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PdfRawTextResponseDto> debugExtractText(
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(adminTestSeriesService.debugExtractText(file));
    }
}

package com.crevolab.testseries.service;

import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.dto.admin.AdminStatsDto;
import com.crevolab.testseries.dto.admin.PdfRawTextResponseDto;
import com.crevolab.testseries.dto.admin.TestRequestDto;
import com.crevolab.testseries.dto.admin.TestResponseDto;
import com.crevolab.testseries.domain.enums.TestStatus;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.repository.AttemptRepository;
import com.crevolab.testseries.repository.TestSeriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminTestSeriesService {

    private final TestSeriesRepository testSeriesRepository;
    private final AttemptRepository attemptRepository;
    private final PdfTextExtractionService pdfTextExtractionService;

    public AdminTestSeriesService(TestSeriesRepository testSeriesRepository,
                                  AttemptRepository attemptRepository,
                                  PdfTextExtractionService pdfTextExtractionService) {
        this.testSeriesRepository = testSeriesRepository;
        this.attemptRepository = attemptRepository;
        this.pdfTextExtractionService = pdfTextExtractionService;
    }

    @Transactional(readOnly = true)
    public AdminStatsDto getAdminStats() {
        long totalTests = testSeriesRepository.count();
        long publishedTests = testSeriesRepository.countByStatus(TestStatus.PUBLISHED);
        long draftTests = testSeriesRepository.countByStatus(TestStatus.DRAFT);
        long totalAttempts = attemptRepository.count();
        return new AdminStatsDto(totalTests, publishedTests, draftTests, totalAttempts);
    }

    public TestResponseDto publishTest(Long id) {
        TestSeries test = testSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));
        test.setStatus(TestStatus.PUBLISHED);
        TestSeries updated = testSeriesRepository.save(test);
        return TestResponseDto.fromEntity(updated);
    }

    public TestResponseDto createTest(TestRequestDto request) {
        TestSeries test = new TestSeries();
        mapDtoToEntity(request, test);
        TestSeries saved = testSeriesRepository.save(test);
        return TestResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TestResponseDto> getAllTests() {
        return testSeriesRepository.findAll()
                .stream()
                .map(TestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestResponseDto getTestById(Long id) {
        TestSeries test = testSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));
        return TestResponseDto.fromEntity(test);
    }

    public TestResponseDto updateTest(Long id, TestRequestDto request) {
        TestSeries test = testSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

        mapDtoToEntity(request, test);
        TestSeries updated = testSeriesRepository.save(test);
        return TestResponseDto.fromEntity(updated);
    }

    public void deleteTest(Long id) {
        if (!testSeriesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Test not found with id: " + id);
        }
        testSeriesRepository.deleteById(id);
    }

    public TestResponseDto uploadPdf(Long id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty");
        }

        // Validate max size: 10 MB = 10 * 1024 * 1024 bytes
        long maxSizeBytes = 10L * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10 MB");
        }

        // Validate extension: .pdf
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Invalid file extension. Only .pdf files are allowed");
        }

        // Validate content-type: application/pdf
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("application/pdf") && !contentType.toLowerCase().startsWith("application/pdf;"))) {
            throw new IllegalArgumentException("Invalid content type. Expected application/pdf");
        }

        // Validate magic bytes: first bytes must be %PDF (0x25, 0x50, 0x44, 0x46)
        byte[] header = new byte[4];
        try (InputStream inputStream = file.getInputStream()) {
            int bytesRead = inputStream.readNBytes(header, 0, 4);
            if (bytesRead < 4 || header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) {
                throw new IllegalArgumentException("Invalid file format. File must start with %PDF magic bytes");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read file content: " + e.getMessage(), e);
        }

        // Verify test exists
        TestSeries test = testSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

        // Save to uploads/ with random UUID filename (never use original name in the path)
        String randomFilename = UUID.randomUUID().toString() + ".pdf";
        Path uploadDirPath = Paths.get("uploads");
        try {
            if (!Files.exists(uploadDirPath)) {
                Files.createDirectories(uploadDirPath);
            }
            Path targetLocation = uploadDirPath.resolve(randomFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save uploaded file: " + e.getMessage(), e);
        }

        // Clean up previous file if present
        if (test.getSourcePdfPath() != null && !test.getSourcePdfPath().isBlank()) {
            try {
                Path oldFile = Paths.get(test.getSourcePdfPath());
                Files.deleteIfExists(oldFile);
            } catch (Exception ignored) {
            }
        }

        // Store path in tests.source_pdf_path
        String storedPath = "uploads/" + randomFilename;
        test.setSourcePdfPath(storedPath);
        TestSeries updated = testSeriesRepository.save(test);

        return TestResponseDto.fromEntity(updated);
    }

    // DEMO-ONLY: Debug endpoint to extract raw text for a test's uploaded PDF
    @Transactional(readOnly = true)
    public PdfRawTextResponseDto getRawExtractedText(Long id) {
        TestSeries test = testSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

        String pdfPath = test.getSourcePdfPath();
        if (pdfPath == null || pdfPath.isBlank()) {
            throw new IllegalArgumentException("No PDF has been uploaded for test id: " + id);
        }

        Path path = Paths.get(pdfPath);
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Uploaded PDF file not found at: " + pdfPath);
        }

        PdfTextExtractionService.ExtractionResult result = pdfTextExtractionService.extractTextWithMetadata(path);
        return new PdfRawTextResponseDto(
                test.getId(),
                test.getSourcePdfPath(),
                result.text(),
                result.pageCount(),
                result.characterCount()
        );
    }

    // DEMO-ONLY: Direct debug text extraction from an uploaded file
    public PdfRawTextResponseDto debugExtractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty");
        }
        try {
            byte[] bytes = file.getBytes();
            PdfTextExtractionService.ExtractionResult result = pdfTextExtractionService.extractTextWithMetadata(bytes);
            return new PdfRawTextResponseDto(
                    null,
                    file.getOriginalFilename(),
                    result.text(),
                    result.pageCount(),
                    result.characterCount()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    private void mapDtoToEntity(TestRequestDto dto, TestSeries entity) {
        entity.setTitle(dto.getTitle());
        entity.setInstructions(dto.getInstructions());
        entity.setDurationMinutes(dto.getDurationMinutes());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : entity.getStatus());

        AccessType accessType = dto.getAccessType() != null ? dto.getAccessType() : AccessType.FREE;
        entity.setAccessType(accessType);

        if (accessType == AccessType.FREE) {
            entity.setPrice(BigDecimal.ZERO);
        } else {
            entity.setPrice(dto.getPrice());
        }

        entity.setMaxAttempts(dto.getMaxAttempts());
        entity.setMaxSolutionViews(dto.getMaxSolutionViews());
        entity.setMaxPdfDownloads(dto.getMaxPdfDownloads());
        entity.setSourcePdfPath(dto.getSourcePdfPath());
        entity.setSolutionPdfPath(dto.getSolutionPdfPath());
    }
}

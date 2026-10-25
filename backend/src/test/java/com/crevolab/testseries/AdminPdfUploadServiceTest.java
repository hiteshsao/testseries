package com.crevolab.testseries;

import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.TestStatus;
import com.crevolab.testseries.dto.admin.TestResponseDto;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.repository.AttemptRepository;
import com.crevolab.testseries.repository.TestSeriesRepository;
import com.crevolab.testseries.service.AdminTestSeriesService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminPdfUploadServiceTest {

    @Mock
    private TestSeriesRepository testSeriesRepository;

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private com.crevolab.testseries.service.PdfTextExtractionService pdfTextExtractionService;

    @InjectMocks
    private AdminTestSeriesService adminTestSeriesService;

    private String uploadedPathToClean;

    @BeforeEach
    void setUp() {
        uploadedPathToClean = null;
    }

    @AfterEach
    void tearDown() {
        if (uploadedPathToClean != null) {
            try {
                Files.deleteIfExists(Paths.get(uploadedPathToClean));
            } catch (IOException ignored) {
            }
        }
    }

    private TestSeries createSampleTest(Long id) {
        TestSeries test = new TestSeries();
        test.setId(id);
        test.setTitle("GATE CS Mock 1");
        test.setDurationMinutes(180);
        test.setStatus(TestStatus.DRAFT);
        test.setAccessType(AccessType.FREE);
        test.setPrice(BigDecimal.ZERO);
        test.setMaxAttempts(1);
        test.setMaxSolutionViews(3);
        test.setMaxPdfDownloads(1);
        return test;
    }

    @Test
    @DisplayName("Valid PDF upload saves to uploads/ with random UUID filename and stores path in test entity")
    void testValidPdfUploadSuccess() throws Exception {
        Long testId = 1L;
        TestSeries test = createSampleTest(testId);

        given(testSeriesRepository.findById(testId)).willReturn(Optional.of(test));
        given(testSeriesRepository.save(any(TestSeries.class))).willAnswer(invocation -> invocation.getArgument(0));

        byte[] pdfContent = "%PDF-1.5\n%Valid test PDF content for Crevolab\n%%EOF".getBytes();
        String originalName = "my-secret-test-paper.pdf";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                originalName,
                "application/pdf",
                pdfContent
        );

        TestResponseDto result = adminTestSeriesService.uploadPdf(testId, file);

        assertThat(result).isNotNull();
        assertThat(result.getSourcePdfPath()).isNotNull();

        String savedPath = result.getSourcePdfPath();
        uploadedPathToClean = savedPath;

        // Path must start with uploads/ and end with .pdf
        assertThat(savedPath).startsWith("uploads/");
        assertThat(savedPath).endsWith(".pdf");

        // The original name must NEVER be part of the path
        assertThat(savedPath).doesNotContain("my-secret-test-paper");

        // File must physically exist on disk and content match
        Path filePath = Paths.get(savedPath);
        assertThat(Files.exists(filePath)).isTrue();
        assertThat(Files.readAllBytes(filePath)).isEqualTo(pdfContent);

        // Test entity sourcePdfPath should be updated
        assertThat(test.getSourcePdfPath()).isEqualTo(savedPath);
    }

    @Test
    @DisplayName("Null or empty file throws IllegalArgumentException")
    void testEmptyFileThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThatThrownBy(() -> adminTestSeriesService.uploadPdf(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File is required and cannot be empty");

        assertThatThrownBy(() -> adminTestSeriesService.uploadPdf(1L, emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File is required and cannot be empty");
    }

    @Test
    @DisplayName("File size exceeding 10 MB throws IllegalArgumentException")
    void testFileExceeding10MbThrowsException() {
        int oversizedBytes = 10 * 1024 * 1024 + 1;
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                new byte[1] // simulate large size with subclass or custom override
        ) {
            @Override
            public long getSize() {
                return oversizedBytes;
            }
        };

        assertThatThrownBy(() -> adminTestSeriesService.uploadPdf(1L, oversizedFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds maximum limit of 10 MB");
    }

    @Test
    @DisplayName("Invalid file extension throws IllegalArgumentException")
    void testInvalidExtensionThrowsException() {
        byte[] content = "%PDF-1.4 dummy".getBytes();

        String[] invalidNames = {"questions.docx", "document.txt", "archive.zip", "test.pdf.exe", "test"};
        for (String name : invalidNames) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    name,
                    "application/pdf",
                    content
            );

            assertThatThrownBy(() -> adminTestSeriesService.uploadPdf(1L, file))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid file extension");
        }
    }

    @Test
    @DisplayName("Invalid content-type throws IllegalArgumentException")
    void testInvalidContentTypeThrowsException() {
        byte[] content = "%PDF-1.4 dummy".getBytes();

        String[] invalidTypes = {"text/plain", "application/octet-stream", "image/png", "application/json"};
        for (String type : invalidTypes) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "sample.pdf",
                    type,
                    content
            );

            assertThatThrownBy(() -> adminTestSeriesService.uploadPdf(1L, file))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid content type");
        }
    }

    @Test
    @DisplayName("Invalid magic bytes (not starting with %PDF) throws IllegalArgumentException")
    void testInvalidMagicBytesThrowsException() {
        byte[] fakeContent = "This is a plain text file pretending to be PDF".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.pdf",
                "application/pdf",
                fakeContent
        );

        assertThatThrownBy(() -> adminTestSeriesService.uploadPdf(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("%PDF magic bytes");
    }

    @Test
    @DisplayName("Non-existent test ID throws ResourceNotFoundException")
    void testTestNotFoundThrowsResourceNotFoundException() {
        Long nonExistentId = 9999L;
        given(testSeriesRepository.findById(nonExistentId)).willReturn(Optional.empty());

        byte[] pdfContent = "%PDF-1.4 valid content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                pdfContent
        );

        assertThatThrownBy(() -> adminTestSeriesService.uploadPdf(nonExistentId, file))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Test not found with id: 9999");
    }
}

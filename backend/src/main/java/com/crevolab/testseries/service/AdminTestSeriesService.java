package com.crevolab.testseries.service;

import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.dto.admin.TestRequestDto;
import com.crevolab.testseries.dto.admin.TestResponseDto;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.repository.TestSeriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminTestSeriesService {

    private final TestSeriesRepository testSeriesRepository;

    public AdminTestSeriesService(TestSeriesRepository testSeriesRepository) {
        this.testSeriesRepository = testSeriesRepository;
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

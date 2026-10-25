package com.crevolab.testseries.repository;

import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.domain.enums.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestSeriesRepository extends JpaRepository<TestSeries, Long> {
    List<TestSeries> findByStatus(TestStatus status);
    long countByStatus(TestStatus status);
}

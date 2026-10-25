package com.crevolab.testseries.repository;

import com.crevolab.testseries.domain.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByStudentIdOrderByStartedAtDesc(String studentId);
    List<Attempt> findByTestIdAndStudentId(Long testId, String studentId);
    long countByTestIdAndStudentId(Long testId, String studentId);
}

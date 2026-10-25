package com.crevolab.testseries.repository;

import com.crevolab.testseries.domain.SolutionAccess;
import com.crevolab.testseries.domain.SolutionAccessId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolutionAccessRepository extends JpaRepository<SolutionAccess, SolutionAccessId> {
    Optional<SolutionAccess> findByIdTestIdAndIdStudentId(Long testId, String studentId);
}

package com.crevolab.testseries.repository;

import com.crevolab.testseries.domain.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByStudentId(String studentId);
    Optional<Purchase> findByTestIdAndStudentId(Long testId, String studentId);
    boolean existsByTestIdAndStudentId(Long testId, String studentId);
}

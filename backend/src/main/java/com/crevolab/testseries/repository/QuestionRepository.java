package com.crevolab.testseries.repository;

import com.crevolab.testseries.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTestIdOrderByOrderNoAsc(Long testId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Question q WHERE q.test.id = :testId")
    void deleteByTestId(@org.springframework.data.repository.query.Param("testId") Long testId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(MAX(q.orderNo), 0) FROM Question q WHERE q.test.id = :testId")
    Integer findMaxOrderNoByTestId(@org.springframework.data.repository.query.Param("testId") Long testId);
}

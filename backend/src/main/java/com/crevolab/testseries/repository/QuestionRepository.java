package com.crevolab.testseries.repository;

import com.crevolab.testseries.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTestIdOrderByOrderNoAsc(Long testId);
}

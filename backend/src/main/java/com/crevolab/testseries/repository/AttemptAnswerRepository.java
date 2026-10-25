package com.crevolab.testseries.repository;

import com.crevolab.testseries.domain.AttemptAnswer;
import com.crevolab.testseries.domain.AttemptAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, AttemptAnswerId> {
    List<AttemptAnswer> findByAttemptId(Long attemptId);
}

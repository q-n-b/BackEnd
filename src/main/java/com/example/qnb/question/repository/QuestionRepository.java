package com.example.qnb.question.repository;

import com.example.qnb.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// QuestionRepository.java
@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    // 특정 도서의 질문 전체 조회
    Page<Question> findByBookId(Integer bookId, Pageable pageable);

    // 최신순 질문 조회
    Page<Question> findByBookIdOrderByCreatedAtDesc(Integer bookId, Pageable pageable);
}

package com.example.qnb.question.repository;

import com.example.qnb.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// QuestionRepository.java
@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    // 특정 도서의 질문 전체 조회
    Page<Question> findByBookId(Integer bookId, Pageable pageable);

    // 최신순 정렬 + GPT 우선
    @Query("""
    SELECT q FROM Question q
    WHERE q.bookId = :bookId
    ORDER BY 
        CASE WHEN q.userNickname = 'GPT' THEN 0 ELSE 1 END, 
        q.createdAt DESC
    """)
    Page<Question> findWithGptTopByBookIdOrderByCreatedAtDesc(
            @Param("bookId") Integer bookId,
            Pageable pageable
    );

    // 인기순 정렬 + GPT 우선
    @Query("""
    SELECT q FROM Question q
    WHERE q.bookId = :bookId
    ORDER BY 
        CASE WHEN q.userNickname = 'GPT' THEN 0 ELSE 1 END, 
        q.likeCount DESC
    """)
    Page<Question> findWithGptTopByBookIdOrderByLikeCountDesc(
            @Param("bookId") Integer bookId,
            Pageable pageable
    );


}

package com.example.qnb.book.repository;

//추천 도서 레포지토리

import com.example.qnb.book.entity.UserRecommendedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

// UserRecommendedBookRepository.java
@Repository
public interface UserRecommendedBookRepository extends JpaRepository<UserRecommendedBook, Integer> {

    // 추천 도서 중 장르 기준으로 필터링
    @Query("SELECT r FROM UserRecommendedBook r WHERE r.book.genre = :genre")
    List<UserRecommendedBook> findByBookGenre(@Param("genre") String genre);

    // 최근 개인 추천 도서 중 가장 최근 1권만 가져오는 메서드
    Optional<UserRecommendedBook> findTopByOrderByRecommendedAtDesc();

}


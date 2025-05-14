package com.example.qnb.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.qnb.book.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    boolean existsByIsbn13(String isbn13); //isbn 중복 검사 메서드
}


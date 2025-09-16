package qnb.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import qnb.book.entity.Book;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    //isbn 중복 검사 메서드
    boolean existsByIsbn13(String isbn13);

    //신간도서 정렬
    Page<Book> findAllByOrderByPublishedYearDesc(Pageable pageable);

    //키워드로 책 검색 (요약 버전)
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    List<Book> findBooksForSummary(@Param("keyword") String keyword);

    //키워드로 책 검색 (full버전)
    /*@Query("SELECT b FROM Book b " +
            "WHERE b.title LIKE %:keyword% " +
            "OR b.author LIKE %:keyword% " +
            "OR b.genre LIKE %:keyword%")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);*/

    @Query("SELECT b FROM Book b " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.genre LIKE %:keyword%)")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);

}


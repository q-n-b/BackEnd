package com.example.qnb.book.service;

import com.example.qnb.book.dto.BookResponseDto;
import com.example.qnb.book.dto.SingleRecommendedBookResponseDto;
import com.example.qnb.book.entity.Book;
import com.example.qnb.book.entity.UserRecommendedBook;
import com.example.qnb.book.repository.BookRepository;
import com.example.qnb.book.repository.UserRecommendedBookRepository;
import com.example.qnb.question.dto.QuestionResponseDto;
import com.example.qnb.question.entity.Question;
import com.example.qnb.question.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserRecommendedBookRepository recommendedBookRepository;
    private final QuestionRepository questionRepository;

    public BookService(BookRepository bookRepository,
                       UserRecommendedBookRepository recommendedBookRepository,
                       QuestionRepository questionRepository) {
        this.bookRepository = bookRepository;
        this.recommendedBookRepository = recommendedBookRepository;
        this.questionRepository = questionRepository;
    }

    //도서 존재 여부 로직
    public boolean existsById(Integer bookId) {
        return bookRepository.existsById(bookId);
    }

    // 0. 개인 추천 도서 1권 조회 (추천 이유 포함)
    public SingleRecommendedBookResponseDto getSingleRecommendedBook() {
        UserRecommendedBook rec = recommendedBookRepository
                .findTopByOrderByRecommendedAtDesc()
                .orElseThrow(() -> new RuntimeException("추천 도서를 찾을 수 없습니다."));

        Book book = rec.getBook();

        return new SingleRecommendedBookResponseDto(
                rec.getBook(),
                rec.getReason(),   // 여기에서 reason 꺼냄
                rec.getKeyword()    // 예: "아포칼립스"
        );
    }


    // 1. 개인 추천 도서 리스트 조회
    public List<BookResponseDto> getRecommendedBooks() {
        List<UserRecommendedBook> list = recommendedBookRepository.findAll();
        return list.stream()
                .map(rec -> new BookResponseDto(rec.getBook()))
                .toList();
    }

    // 2. 장르별 추천 도서 리스트 조회
    public List<BookResponseDto> getRecommendedBooksByGenre(String genre) {
        List<UserRecommendedBook> list = recommendedBookRepository.findByBookGenre(genre);
        return list.stream()
                .map(rec -> new BookResponseDto(rec.getBook()))
                .toList();
    }

    // 3. 신간 도서 리스트 조회 (출간일 내림차순)
    public Page<BookResponseDto> getNewBooks(Pageable pageable) {
        return bookRepository.findAllByOrderByPublishedYearDesc(pageable)
                .map(BookResponseDto::new);
    }

    // 4. 도서 상세 조회
    public BookResponseDto getBookDetail(Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("도서를 찾을 수 없습니다."));
        return new BookResponseDto(book);
    }

    // 5. 특정 도서의 질문 리스트 조회
    public Page<QuestionResponseDto> getBookQuestions(Integer bookId, String sort, Pageable pageable) {
        Page<Question> questions;

        if ("popular".equals(sort)) {
            questions = questionRepository.findWithGptTopByBookIdOrderByLikeCountDesc(bookId, pageable);
        } else {
            questions = questionRepository.findWithGptTopByBookIdOrderByCreatedAtDesc(bookId, pageable);
        }

        return questions.map(QuestionResponseDto::new);
    }


}

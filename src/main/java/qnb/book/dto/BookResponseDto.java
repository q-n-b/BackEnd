package qnb.book.dto;

import qnb.book.entity.Book;
import lombok.Data;

@Data
public class BookResponseDto {

    private Integer bookId;        // 도서 ID
    private String title;          // 제목
    private String author;         // 저자
    private String genre;          // 장르
    private Integer publishedYear; // 출간 연도
    private String imageUrl;       // 이미지 URL
    private String publisher;      // 출판사
    private String isbn13;         // ISBN
    private String description;    // 설명
    private String scrapCount;

    // 생성자
    public BookResponseDto(Book book) {
        this.bookId = book.getBookId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.genre = book.getGenre();
        this.publishedYear = book.getPublishedYear();
        this.imageUrl = book.getImageUrl();
        this.publisher = book.getPublisher();
        this.isbn13 = book.getIsbn13();
        this.description = book.getDescription();
    }

    public static BookResponseDto from(Book book) {
        return new BookResponseDto(book);
    }
}

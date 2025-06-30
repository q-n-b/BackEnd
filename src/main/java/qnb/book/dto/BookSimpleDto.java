package qnb.book.dto;

//단순히 bookId와 제목만 리턴해주는 DTO

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import qnb.book.entity.Book;

@Getter
@Builder
@AllArgsConstructor
public class BookSimpleDto {
    private Integer bookId;
    private String bookTitle;
    private String imageUrl;
    private String author;
    private String publisher;
    private Integer publishedYear;

    public static BookSimpleDto from(Book book) {
        if (book == null) {
            return null;
        }

        return BookSimpleDto.builder()
                .bookId(book.getBookId() != null ? book.getBookId() : null)
                .bookTitle(book.getTitle() != null ? book.getTitle() : "")
                .imageUrl(book.getImageUrl() != null ? book.getImageUrl() : "")
                .author(book.getAuthor() != null ? book.getAuthor() : "")
                .publisher(book.getPublisher() != null ? book.getPublisher() : "")
                .publishedYear(book.getPublishedYear() != null ? book.getPublishedYear() : null)
                .build();
    }
}


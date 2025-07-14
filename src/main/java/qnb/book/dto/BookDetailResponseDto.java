package qnb.book.dto;
//책 상세 조회에서 쓰이는 DTO

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import qnb.book.entity.Book;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BookDetailResponseDto {
    private Integer bookId;
    private String title;
    private String author;
    private String genre;
    private Integer publishedYear;
    private String imageUrl;
    private String publisher;
    private String isbn13;
    private String description;

    @JsonProperty("isScrapped")
    private boolean isScrapped;
    private String scrapStatus;   // "wish" | "reading" | "read" | null

    public static BookDetailResponseDto from(Book book, boolean isScrapped, String scrapStatus) {
        return BookDetailResponseDto.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .publishedYear(book.getPublishedYear())
                .imageUrl(book.getImageUrl())
                .publisher(book.getPublisher())
                .isbn13(book.getIsbn13())
                .description(book.getDescription())
                .isScrapped(isScrapped)
                .scrapStatus(scrapStatus)
                .build();
    }
}

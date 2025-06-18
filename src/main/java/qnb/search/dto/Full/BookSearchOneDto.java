package qnb.search.dto.Full;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.book.entity.Book;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchOneDto {
    private Long bookId;
    private String isbn13;
    private String title;
    private String author;
    private int publishedYear;
    private String publisher;
    private String imageUrl;
    private String genre;
    private int questionCount;
    private int scrapCount;

    public static BookSearchOneDto from(Book book, int scrapCount) {
        return new BookSearchOneDto(
                book.getBookId().longValue(),  // 만약 Integer면 longValue() 필요
                book.getIsbn13(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublishedYear(),
                book.getPublisher(),
                book.getImageUrl(),
                book.getGenre(),
                book.getQuestionCount(),
                scrapCount
        );
    }

}





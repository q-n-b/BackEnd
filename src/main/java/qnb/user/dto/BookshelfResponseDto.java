package qnb.user.dto;
//나의 책장

import qnb.book.entity.Book;

public record BookshelfResponseDto(
        Integer bookId,
        String title,
        String author,
        String imageUrl,
        String status
) {
    public static BookshelfResponseDto from(Book book, String status) {
        return new BookshelfResponseDto(
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getImageUrl(),
                status
        );
    }
}


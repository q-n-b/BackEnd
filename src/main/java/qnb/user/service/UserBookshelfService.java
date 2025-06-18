package qnb.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import qnb.user.dto.BookshelfResponseDto;
import qnb.user.entity.User;
import qnb.user.repository.UserBookReadRepository;
import qnb.user.repository.UserBookReadingRepository;
import qnb.user.repository.UserBookWishRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserBookshelfService {

    private final UserBookReadRepository readRepo;
    private final UserBookReadingRepository readingRepo;
    private final UserBookWishRepository wishRepo;

    public List<BookshelfResponseDto> getBooksByStatus(User user, String status) {
        Long userId = user.getUserId();

        return switch (status.toUpperCase()) {
            case "READ" -> {
                var books = readRepo.findByUser_UserId(userId);
                System.out.println(" READ 책 개수: " + books.size());
                yield books.stream()
                        .map(r -> BookshelfResponseDto.from(r.getBook(), "READ"))
                        .toList();
            }
            case "READING" -> {
                var books = readingRepo.findByUser_UserId(userId);
                System.out.println("READING 책 개수: " + books.size());
                yield books.stream()
                        .map(r -> BookshelfResponseDto.from(r.getBook(), "READING"))
                        .toList();
            }
            case "WISH" -> {
                var books = wishRepo.findByUser_UserId(userId);
                System.out.println("WISH 책 개수: " + books.size());
                yield books.stream()
                        .map(r -> BookshelfResponseDto.from(r.getBook(), "WISH"))
                        .toList();
            }
            default -> throw new IllegalArgumentException(" 지원하지 않는 status 값입니다: " + status);
        };
    }
}
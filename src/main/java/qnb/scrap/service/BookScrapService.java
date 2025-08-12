package qnb.scrap.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import qnb.scrap.dto.BookScrapResponseDto;
import qnb.book.entity.Book;
import qnb.book.repository.BookRepository;
import qnb.common.exception.InvalidStatusException;
import qnb.common.exception.UserNotFoundException;
import qnb.user.entity.User;
import qnb.user.entity.UserBookRead;
import qnb.user.entity.UserBookReading;
import qnb.user.entity.UserBookWish;
import qnb.user.repository.UserBookWishRepository;
import qnb.user.repository.UserBookReadRepository;
import qnb.user.repository.UserBookReadingRepository;
import qnb.common.exception.BookNotFoundException;
import qnb.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import qnb.user.event.UserBookReadAddedEvent;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookScrapService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserBookWishRepository userBookWishRepository;
    private final UserBookReadingRepository userBookReadingRepository;
    private final UserBookReadRepository userBookReadRepository;

    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public BookScrapResponseDto toggleScrap(Integer bookId, Long userId, String status) {
        status = status.toUpperCase();

        if (!Set.of("WISH", "READING", "READ").contains(status)) {
            throw new InvalidStatusException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);

        // 현재 상태 확인
        boolean isWish = userBookWishRepository.existsByUser_UserIdAndBook_BookId(userId, bookId);
        boolean isReading = userBookReadingRepository.existsByUser_UserIdAndBook_BookId(userId, bookId);
        boolean isReadBefore = userBookReadRepository.existsByUser_UserIdAndBook_BookId(userId, bookId);

        // 동일 상태면 토글 해제
        if ((status.equals("WISH") && isWish) ||
                (status.equals("READING") && isReading) ||
                (status.equals("READ") && isReadBefore)) {

            deleteScrapStatus(userId, bookId);
            return BookScrapResponseDto.builder()
                    .bookId(bookId)
                    .scrapStatus(null)
                    .message("도서 스크랩이 취소되었습니다.")
                    .build();
        }

        // 기존 상태 제거
        deleteScrapStatus(userId, bookId);

        boolean readInsertedThisTx = false; // 이번 호출에서 READ가 새로 추가됐는지

        // 새로운 상태 등록
        switch (status) {
            case "WISH" -> userBookWishRepository.save(
                    UserBookWish.builder().user(user).book(book).
                            createdAt(LocalDateTime.now()).build()
            );
            case "READING" -> userBookReadingRepository.save(
                    UserBookReading.builder().user(user).book(book).
                            createdAt(LocalDateTime.now()).build()
            );
            case "READ" -> {
                // 동시요청 대비 한 번 더 확인
                if (!userBookReadRepository.existsByUser_UserIdAndBook_BookId(userId, bookId)) {
                    userBookReadRepository.save(
                            UserBookRead.builder().user(user).book(book).
                                    createdAt(LocalDateTime.now()).build()
                    );
                    readInsertedThisTx = true;
                }
            }
        }

        // READ가 "신규"로 들어간 경우에만 이벤트 발행 (커밋 후 비동기 처리됨)
        if ("READ".equals(status) && readInsertedThisTx && !isReadBefore) {
            eventPublisher.publishEvent(new UserBookReadAddedEvent(userId));
        }

        return BookScrapResponseDto.builder()
                .bookId(bookId)
                .scrapStatus(status)
                .message("도서 스크랩 상태가 저장되었습니다.")
                .build();
    }

    private void deleteScrapStatus(Long userId, Integer bookId) {
        userBookWishRepository.deleteByUser_UserIdAndBook_BookId(userId, bookId);
        userBookReadingRepository.deleteByUser_UserIdAndBook_BookId(userId, bookId);
        userBookReadRepository.deleteByUser_UserIdAndBook_BookId(userId, bookId);
    }

    //삭제 메소드
    @Transactional
    public void deleteScrap(Long userId, Integer bookId) {
        // 도서 존재 여부 체크
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException();
        }

        // 유저 존재 여부 체크
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        // 현재 상태 확인
        boolean isWish = userBookWishRepository.existsByUser_UserIdAndBook_BookId(userId, bookId);
        boolean isReading = userBookReadingRepository.existsByUser_UserIdAndBook_BookId(userId, bookId);
        boolean isRead = userBookReadRepository.existsByUser_UserIdAndBook_BookId(userId, bookId);

        // 아무 스크랩도 없다면 예외
        if (!isWish && !isReading && !isRead) {
            throw new qnb.common.exception.ScrapNotFoundException();
        }

        // 삭제
        deleteScrapStatus(userId, bookId);
    }
}

package qnb.user.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import qnb.book.entity.Book;
import qnb.book.repository.BookRepository;
import qnb.user.dto.UserPreferenceRequestDto;
import qnb.user.entity.User;
import qnb.user.entity.UserBookRead;
import qnb.user.entity.UserPreference;
import qnb.user.event.UserBookReadAddedEvent;
import qnb.user.repository.UserBookReadRepository;
import qnb.user.repository.UserPreferenceRepository;
import qnb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final BookRepository bookRepository;
    private final UserBookReadRepository userBookReadRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void savePreference(Long userId, UserPreferenceRequestDto dto) {

        //사용자 조회/검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getHasReadingTaste()) {
            throw new AccessDeniedException("이미 취향조사를 완료한 사용자입니다.");
        }

        // 취향 메타 저장
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setReadingAmount(dto.getReadingAmount());
        preference.setImportantFactor(dto.getImportantFactor());
        preference.setPreferredGenres(dto.getPreferredGenres());
        preference.setPreferredKeywords(dto.getPreferredKeywords());
        preferenceRepository.save(preference);

        // 설문에서 선택한 책을 READ로 저장 (중복 방지)
        boolean anyReadInserted = false;
        if (dto.getPreferredBookId() != null && !dto.getPreferredBookId().isEmpty()) {
            for (Integer bookId : dto.getPreferredBookId()) {
                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

                // 이미 READ면 스킵
                boolean alreadyRead = userBookReadRepository
                        .existsByUser_UserIdAndBook_BookId(userId, bookId);
                if (alreadyRead) continue;

                userBookReadRepository.save(
                        UserBookRead.builder()
                                .user(user)
                                .book(book)
                                .source("SURVEY")
                                .createdAt(LocalDateTime.now())
                                .build()
                );
                anyReadInserted = true;
            }
        }

        // 플래그 업데이트
        user.setHasReadingTaste(true);
        userRepository.save(user);

        //  이번 트랜잭션에서 READ가 새로 들어간 경우에만 이벤트 발행
        if (anyReadInserted) {
            eventPublisher.publishEvent(new UserBookReadAddedEvent(userId));
        }
    }
}
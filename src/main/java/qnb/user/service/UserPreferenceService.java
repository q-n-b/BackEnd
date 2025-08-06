package qnb.user.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import qnb.book.entity.Book;
import qnb.book.repository.BookRepository;
import qnb.user.dto.UserPreferenceRequestDto;
import qnb.user.entity.User;
import qnb.user.entity.UserBookRead;
import qnb.user.entity.UserPreference;
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

    @Transactional
    public void savePreference(Long userId, UserPreferenceRequestDto dto) {
        log.info("🎯 preferredBookId: {}", dto.getPreferredBookId());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 취향조사 완료 시 차단
        if (user.getHasReadingTaste()) {
            throw new AccessDeniedException("이미 취향조사를 완료한 사용자입니다.");
        }

        // 1. UserPreference 엔티티 저장 (취향 메타데이터)
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setReadingAmount(dto.getReadingAmount());
        preference.setImportantFactor(dto.getImportantFactor());
        preference.setPreferredGenres(dto.getPreferredGenres());
        preference.setPreferredKeywords(dto.getPreferredKeywords());
        preferenceRepository.save(preference);

        // 2.설문에서 선택한 책을 user_book_read에 바로 저장
        if (dto.getPreferredBookId() != null && !dto.getPreferredBookId().isEmpty()) {
            for (Integer bookId : dto.getPreferredBookId()) {
                log.info("🎯 저장 시도 bookId = {}", bookId);
                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

                UserBookRead read = UserBookRead.builder()
                        .user(user)
                        .book(book)
                        .source("SURVEY") // 설문에서 선택한 책임을 표시
                        .createdAt(LocalDateTime.now())
                        .build();

                userBookReadRepository.save(read);
            }
        } else {
            log.warn("⚠️ preferredBookId가 비어 있어서 user_book_read insert 안 됨");
        }

        // 3. 유저 플래그 업데이트
        user.setHasReadingTaste(true);
        userRepository.save(user);
    }
}

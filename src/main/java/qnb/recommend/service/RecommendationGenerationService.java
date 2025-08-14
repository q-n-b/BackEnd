package qnb.recommend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import qnb.book.entity.Book;
import qnb.book.entity.UserRecommendedBook;
import qnb.book.repository.BookRepository;
import qnb.book.repository.UserRecommendedBookRepository;
import qnb.common.exception.*;
import qnb.recommend.client.MlRecommendClient;
import qnb.recommend.dto.MlRecommendRequest;
import qnb.recommend.dto.MlRecommendResponse;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationGenerationService {

    private final MlRecommendClient mlClient;
    private final UserRecommendedBookRepository urbRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // 재사용

    @Transactional
    public MlRecommendResponse generateAndPersist(String bearerToken, int topK, MlRecommendRequest req) {

        // 401
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new UnauthorizedAccessException();
        }

        // 400
        if (req == null || req.getUserId() == null || topK <= 0 || topK > 100) {
            throw new InvalidRequestException("요청 형식이 올바르지 않습니다.");
        }

        // 1) ML 호출 (네트워크 예외 → 500)
        MlRecommendResponse resp;
        try {
            resp = mlClient.recommendBooks(bearerToken, topK, req);
        } catch (Exception e) {
            throw new InternalErrorException("추천 생성 중 오류가 발생했습니다.");
        }

        // 404: 추천 없음/빈 응답
        if (resp == null || resp.getItems() == null || resp.getItems().isEmpty()) {
            throw new UserNotIndexedException("해당 사용자는 추천 인덱스에 존재하지 않습니다.");
        }

        // User는 루프 밖에서 1회 조회
        User userEntity = userRepository.findById(req.getUserId())
                .orElseThrow(UserNotFoundException::new);


        // 2) 기존 추천은 "신규 저장 가능"이 확정된 이후 삭제
        urbRepository.deleteByUserId(userEntity.getUserId());

        // 3) 매핑/저장
        List<UserRecommendedBook> batch = new ArrayList<>();

        for (MlRecommendResponse.Item item : resp.getItems()) {
            Long bookId = item.getBookId();
            if (bookId == null) continue;

            // 한 번의 findById로 존재 확인 + 엔티티 획득
            Book bookEntity = bookRepository.findById(bookId.intValue())
                    .orElse(null);
            if (bookEntity == null) continue; // 무결성 방어

            String keywordsJson = null;
            if (item.getKeywords() != null) {
                try {
                    keywordsJson = objectMapper.writeValueAsString(item.getKeywords());
                } catch (JsonProcessingException ignored) {}
            }

            UserRecommendedBook urb = UserRecommendedBook.builder()
                    .user(userEntity)
                    .book(bookEntity)
                    .keyword(keywordsJson)
                    .recommendedAt(
                            resp.getGeneratedAt() != null
                                    ? LocalDateTime.ofInstant(resp.getGeneratedAt(), ZoneId.of("UTC"))
                                    : LocalDateTime.now(ZoneId.of("UTC"))
                    )
                    .build();

            batch.add(urb);
        }

        if (!batch.isEmpty()) {
            urbRepository.saveAll(batch);
        }
        return resp;
    }
}

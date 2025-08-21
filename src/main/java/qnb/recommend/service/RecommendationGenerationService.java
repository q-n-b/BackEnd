package qnb.recommend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

        String stage = "INIT";
        log.info("[RecommendSvc] START stage={}, userId={}, topK={}, auth={}",
                stage, req != null ? req.getUserId() : null, topK, mask(bearerToken));

        // 401
        stage = "VALIDATE_INPUT_AUTH";
        if (bearerToken == null || bearerToken.isBlank()) {
            log.warn("[RecommendSvc] FAIL stage={} (empty Authorization)", stage);
            throw new UnauthorizedAccessException();
        }

        // 400
        stage = "VALIDATE_INPUT_REQ";
        if (req == null || req.getUserId() == null || topK <= 0 || topK > 100) {
            log.warn("[RecommendSvc] FAIL stage={} (invalid request) userId={}, topK={}",
                    stage, req != null ? req.getUserId() : null, topK);
            throw new InvalidRequestException("요청 형식이 올바르지 않습니다.");
        }

        // 1) ML 호출 (네트워크 예외 → 500)
        stage = "CALL_ML";
        MlRecommendResponse resp;
        final long t0 = System.currentTimeMillis();
        try {
            log.info("[RecommendSvc] CALL ML stage={} userId={}, topK={}", stage, req.getUserId(), topK);
            resp = mlClient.recommendBooks(bearerToken, topK, req);
            log.info("[RecommendSvc] CALL ML DONE in {}ms", System.currentTimeMillis() - t0);
        } catch (Exception e) {
            log.error("[RecommendSvc] FAIL stage={} (ML call) msg={}", stage, e.getMessage(), e);
            throw new InternalErrorException("추천 생성 중 오류가 발생했습니다.");
        }

        // 404: 추천 없음/빈 응답
        stage = "VALIDATE_ML_RESPONSE";
        if (resp == null || resp.getItems() == null || resp.getItems().isEmpty()) {
            log.warn("[RecommendSvc] FAIL stage={} (empty ML response) userId={}", stage,
                    req.getUserId());
            throw new UserNotIndexedException("해당 사용자는 추천 인덱스에 존재하지 않습니다.");
        }
        log.info("[RecommendSvc] ML RESPONSE stage={} itemsCount={}", stage, resp.getItems().size());

        // User는 루프 밖에서 1회 조회
        stage = "LOAD_USER";
        final String stageSnapshot = stage; // ← effectively final 스냅샷
        User userEntity = userRepository.findById(req.getUserId())
                .orElseThrow(() -> {
                    log.warn("[RecommendSvc] FAIL stage={} (user not found) userId={}", stageSnapshot, req.getUserId());
                    return new UserNotFoundException();
                });
        log.info("[RecommendSvc] USER LOADED stage={} userId={}", stage, userEntity.getUserId());

        // 2) 기존 추천 삭제
        stage = "DELETE_OLD_RECOMMENDATIONS";
        urbRepository.deleteByUserId(userEntity.getUserId());
        log.info("[RecommendSvc] OLD RECOMMENDATIONS DELETED stage={} userId={}", stage,
                userEntity.getUserId());

        // 3) 매핑/저장
        stage = "MAP_AND_BUILD_BATCH";
        List<UserRecommendedBook> batch = new ArrayList<>();
        for (MlRecommendResponse.Item item : resp.getItems()) {
            Long bookId = item.getBookId();
            if (bookId == null) continue;

            Book bookEntity = bookRepository.findById(bookId.intValue()).orElse(null);
            if (bookEntity == null) {
                log.warn("[RecommendSvc] SKIP missing bookId={}", bookId);
                continue;
            }

            String keywordsJson = null;
            if (item.getKeywords() != null) {
                try {
                    keywordsJson = objectMapper.writeValueAsString(item.getKeywords());
                } catch (JsonProcessingException ignored) {
                    log.warn("[RecommendSvc] keywords JSON serialize failed for bookId={}", bookId);
                }
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
        log.info("[RecommendSvc] BATCH BUILT stage={} size={}", stage, batch.size());

        stage = "PERSIST";
        if (!batch.isEmpty()) {
            urbRepository.saveAll(batch);
            log.info("[RecommendSvc] PERSISTED stage={} count={}", stage, batch.size());
        } else {
            log.warn("[RecommendSvc] SKIP PERSIST stage={} (empty batch)", stage);
        }

        stage = "DONE";
        log.info("[RecommendSvc] END stage={} userId={} itemsCount={}",
                stage, userEntity.getUserId(), resp.getItems().size());
        return resp;
    }

    // --- logging util (마스킹) ---
    private String mask(String auth) {
        if (auth == null || auth.isBlank()) return "null";
        String t = auth.replaceFirst("(?i)^Bearer\\s+", "");
        return "Bearer " + (t.length() > 8 ? t.substring(0, 4) + "…" + t.substring(t.length() - 4) : "****");
    }
}

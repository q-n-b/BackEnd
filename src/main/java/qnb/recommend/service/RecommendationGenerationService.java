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
import qnb.common.exception.BookNotFoundException;
import qnb.common.exception.InvalidRequestException;
import qnb.common.exception.UnauthorizedAccessException;
import qnb.common.exception.UserNotFoundException;
import qnb.recommend.client.MlRecommendClient;
import qnb.recommend.dto.MlRecommendRequest;
import qnb.recommend.dto.MlRecommendResponse;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

//책 추천 서비스
@Service
@RequiredArgsConstructor
public class RecommendationGenerationService {

    private final MlRecommendClient mlClient;
    private final UserRecommendedBookRepository urbRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public MlRecommendResponse generateAndPersist(String bearerToken, int topK, MlRecommendRequest req) {

        // 401 Unauthorized – 토큰 누락/만료
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new UnauthorizedAccessException();
        }

        // 400 Bad Request – 요청 형식 오류
        if (req == null || req.getUserId() == null || topK <= 0) {
            throw new InvalidRequestException("요청 형식이 올바르지 않습니다.");
        }

        // 기본값 보정
        if (req.getFilters() == null) {
            req.setFilters(MlRecommendRequest.Filters.builder().excludeRead(true).build());
        } else if (req.getFilters().getExcludeRead() == null) {
            req.getFilters().setExcludeRead(true);
        }

        // 1) ML 호출
        MlRecommendResponse resp = mlClient.recommendBooks(bearerToken, topK, req);

        // 2) 기존 추천 삭제 (스냅샷 교체 전략)
        urbRepository.deleteByUserId(req.getUserId());

        // 3) 응답 매핑 후 저장
        int rank = 1;
        List<UserRecommendedBook> batch = new ArrayList<>();
        for (MlRecommendResponse.Item item : resp.getItems()) {
            Long bookId = item.getBookId();
            if (bookId == null) continue;
            if (!bookRepository.existsById(bookId.intValue())) continue; // 무결성 방어

            // 키워드 저장: JSON 문자열로 저장(예: ["관계","심리"])
            String keywordsJson = null;
            if (item.getKeywords() != null) {
                try {
                    keywordsJson = new ObjectMapper().writeValueAsString(item.getKeywords());
                } catch (JsonProcessingException ignored) {}
            }

            User userEntity = userRepository.findById(req.getUserId())
                    .orElseThrow(UserNotFoundException::new);

            Book bookEntity = bookRepository.findById(bookId.intValue())
                    .orElseThrow(BookNotFoundException::new);

            UserRecommendedBook urb = UserRecommendedBook.builder()
                    .user(userEntity)
                    .book(bookEntity)
                    .keyword(keywordsJson)
                    .recommendedAt(resp.getGeneratedAt() != null
                            ? LocalDateTime.ofInstant(resp.getGeneratedAt(), ZoneId.of("UTC"))
                            : LocalDateTime.now(ZoneId.of("UTC")))
                    .build();

            batch.add(urb);
        }
        urbRepository.saveAll(batch);

        return resp;
    }
}

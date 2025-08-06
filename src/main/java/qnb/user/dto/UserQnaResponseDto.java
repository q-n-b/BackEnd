package qnb.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.answer.dto.AnswerSimpleDto;
import qnb.answer.entity.Answer;
import qnb.book.dto.BookResponseDto;
import qnb.question.entity.Question;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserQnaResponseDto {

    private Long questionId;
    private BookResponseDto book;
    private Long userId;
    private String userNickname;
    private String profileUrl;
    private String questionContent;
    private List<AnswerSimpleDto> answers;
    private LocalDateTime createdAt;
    private String type; // "QUESTION" 또는 "ANSWER"

    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

    public static UserQnaResponseDto fromQuestion(Question question, List<Answer> answers) {
        return UserQnaResponseDto.builder()
                .questionId(Long.valueOf(question.getQuestionId()))
                .book(BookResponseDto.from(question.getBook()))
                .userId(question.getUser().getUserId())
                .userNickname(question.getUser().getUserNickname())

                // profileUrl이 null이면 디폴트 이미지 사용
                .profileUrl(
                        question.getUser().getProfileUrl() != null
                                ? question.getUser().getProfileUrl()
                                : DEFAULT_PROFILE_URL
                )
                .questionContent(question.getQuestionContent())
                .answers(answers.stream().map(AnswerSimpleDto::from).toList())
                .createdAt(question.getCreatedAt())
                .type("QUESTION")
                .build();
    }

    public static UserQnaResponseDto fromAnswer(Question question, List<Answer> answers) {
        return fromQuestion(question, answers).toBuilder()
                .type("ANSWER")
                .build();
    }
}

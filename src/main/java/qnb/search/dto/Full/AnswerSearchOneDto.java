package qnb.search.dto.Full;
//검색된 답변 1개에 대한 정보

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.book.dto.BookSimpleDto;
import qnb.question.dto.QuestionSimpleDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSearchOneDto {
    private Long answerId;
    private String answerContent;
    private QuestionSimpleDto question;
    private BookSimpleDto book;
    private int likeCount;
    private String userNickname;
    private String profileUrl;
    private String answerState;

    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = (profileUrl != null && !profileUrl.isEmpty())
                ? profileUrl
                : DEFAULT_PROFILE_URL;
    }
}

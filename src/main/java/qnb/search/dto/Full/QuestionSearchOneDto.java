package qnb.search.dto.Full;
//검색된 질문 1개에 대한 정보

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.book.dto.BookSimpleDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSearchOneDto {
    private Long questionId;
    private String questionContent;
    private BookSimpleDto book;
    private int answerCount;
    private int likeCount;
    private int scrapCount;
    private String userNickname;
    private String profileUrl;

    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

    /**
     * profileUrl을 세팅할 때 null 또는 빈 문자열이면 디폴트 이미지 URL로 대체
     */
    public void setProfileUrl(String profileUrl) {
        this.profileUrl = (profileUrl != null && !profileUrl.isEmpty())
                ? profileUrl
                : DEFAULT_PROFILE_URL;
    }
}

package qnb.answer.dto;

//AnswersByUserDto 생성 시 User user에서 직접 값을 꺼내는 방식

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.user.entity.User;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AnswersByUserDto {
    private Long userId;
    private String userNickname;
    private String profileUrl;
    private List<AnswerResponseDto> answers;

    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

    public AnswersByUserDto(User user, List<AnswerResponseDto> answers) {
        this.userId = user.getUserId();
        this.userNickname = user.getUserNickname();
        // profileUrl이 null이면 디폴트 이미지 사용
        this.profileUrl = (user.getProfileUrl() != null && !user.getProfileUrl().isEmpty())
                ? user.getProfileUrl()
                : DEFAULT_PROFILE_URL;
        this.answers = answers;
    }
}

package qnb.user.dto;

//사용자 정보 반환해주는 DTO

import lombok.Getter;
import qnb.user.entity.Gender;
import qnb.user.entity.User;

import java.time.LocalDate;

@Getter
public class UserInfoResponseDto {
    private Long userId;
    private String userEmail;
    private String userNickname;
    private String profileUrl;
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private String phoneNumber;
    private boolean hasReadingTaste;

    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

    public UserInfoResponseDto(User user) {
        this.userId = user.getUserId();
        this.userEmail = user.getUserEmail();
        this.userNickname = user.getUserNickname();

        // profile_url이 null이면 디폴트 이미지 사용
        this.profileUrl = (user.getProfileUrl() != null)
                ? user.getProfileUrl()
                : DEFAULT_PROFILE_URL;

        this.name = user.getName();
        this.birthDate = user.getBirthDate();
        this.gender = user.getGender();
        this.phoneNumber = user.getPhoneNumber();

        //readingTaste가 null 또는 빈 문자열이면 false, 그렇지 않으면 true로 반환되게 구현
        this.hasReadingTaste = user.getHasReadingTaste();
    }
}

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
    //readingTaste가 null 또는 빈 문자열이면 false, 그렇지 않으면 true로 반환되게 구현
    private boolean hasReadingTaste;

    public UserInfoResponseDto(User user) {
        this.userId = user.getUserId();
        this.userEmail = user.getUserEmail();
        this.userNickname = user.getUserNickname();
        this.profileUrl = user.getProfileUrl();
        this.name = user.getName();
        this.birthDate = user.getBirthDate();
        this.gender = user.getGender();
        this.phoneNumber = user.getPhoneNumber();
        this.hasReadingTaste = user.getReadingTaste() != null && !user.getReadingTaste().isBlank();
    }
}

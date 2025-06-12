package com.example.qnb.user.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import com.example.qnb.user.entity.Gender;

import java.time.LocalDate;

@Getter
@Setter
public class SignupRequestDto {

    @Email
    @NotBlank
    private String userEmail;

    @NotBlank
    private String userPassword;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String name;

    private String userNickname; //닉네임은 필수 아님

    @NotNull
    private LocalDate birthDate;

    @NotBlank
    private Gender gender;

    @NotBlank
    private String phoneNumber;

    private String profileUrl; //서버에서 주입 예정
}






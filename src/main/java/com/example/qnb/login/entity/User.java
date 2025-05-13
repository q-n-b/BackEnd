package com.example.qnb.login.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "`USER`")  // 예약어 우려 줄이기 위해 백틱 사용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String userEmail;

    @Column(nullable = false)
    private String userPassword;

    @Column(nullable = false)
    private String name;

    private String userNickname; //닉네임은 필수 아님

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING) //성별은 남,여로만 제한
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String phoneNumber;

    private String readingTaste;

    private String profileUrl;

}
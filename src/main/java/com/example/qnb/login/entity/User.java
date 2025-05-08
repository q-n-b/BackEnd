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


    @Column(nullable = false)
    private String userPassword;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String userEmail;

    private String userNickname;
    private LocalDate birthDate;
    private String gender;
    private String phoneNumber;
    private String readingTaste;
    private String profileUrl;


    public Long getUserId() {
        return userId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }


    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

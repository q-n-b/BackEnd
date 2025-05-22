package com.example.qnb.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    private String userEmail;

    @Column(nullable = false)
    private String token;

    public RefreshToken(String userEmail, String token) {
        this.userEmail = userEmail;
        this.token = token;
    }
}

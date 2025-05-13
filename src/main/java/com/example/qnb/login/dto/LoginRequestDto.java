package com.example.qnb.login.dto;

public class LoginRequestDto {
    private String userEmail;
    private String userPassword;

    // 생성자 (선택)
    public LoginRequestDto() {}

    public LoginRequestDto(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    // Getter
    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    // Setter (선택, @RequestBody 매핑 시 필요)
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}

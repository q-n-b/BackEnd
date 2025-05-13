package com.example.qnb.login.repository;

import com.example.qnb.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기 (userEmail 필드에 맞춤)
    Optional<User> findByUserEmail(String userEmail);
}

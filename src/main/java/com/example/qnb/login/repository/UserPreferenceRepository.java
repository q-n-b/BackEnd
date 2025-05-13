package com.example.qnb.login.repository;

import com.example.qnb.login.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
}

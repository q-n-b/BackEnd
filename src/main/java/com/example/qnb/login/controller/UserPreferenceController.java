package com.example.qnb.login.controller;

import com.example.qnb.login.dto.UserPreferenceRequestDto;
import com.example.qnb.login.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/preference")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    @PostMapping
    public ResponseEntity<String> save(@RequestBody UserPreferenceRequestDto dto) {
        preferenceService.savePreference(dto); //설문 내용 저장
        return ResponseEntity.ok("회원 취향 저장 완료"); //프론트에 메세지 전달
    }
}

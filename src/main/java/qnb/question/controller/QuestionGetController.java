package com.example.qnb.question.controller;

import com.example.qnb.question.dto.QuestionPageResponseDto;
import com.example.qnb.question.dto.QuestionResponseDto;
import com.example.qnb.question.entity.Question;
import com.example.qnb.user.security.UserDetailsImpl;
import com.example.qnb.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionGetController {

    private final QuestionService questionService;

    //최신 질문 조회
    @GetMapping("/recent")
    public ResponseEntity<QuestionPageResponseDto> getRecentQuestions(
            @RequestParam(defaultValue = "1") int page, //page 기본값 1
            @RequestParam(defaultValue = "5") int size, //page당 질문수 기본값 5
            @AuthenticationPrincipal UserDetailsImpl userDetails) {


        QuestionPageResponseDto response = questionService.getRecentQuestions(page, size);
        return ResponseEntity.ok(response);
    }
}

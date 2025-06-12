package com.example.qnb.user.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data //Lombok의 어노테이션들 다 포함
public class UserPreferenceRequestDto {
    @NotNull
    private Integer readingAmount; //독서량,단일선택

    @NotNull
    private Integer importantFactor; //중요요소,단일선택

    @NotNull
    @Size(min = 1)
    private List<Integer> preferredGenres; //선호장르,복수선택

    private List<String> preferredKeywords;//키워드, 복수선택

    private List<Integer> bookId; //좋았던 책, 복수선택
}

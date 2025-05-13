package com.example.qnb.login.dto;

import lombok.Data;
import java.util.List;

@Data //Lombok의 어노테이션들 다 포함
public class UserPreferenceRequestDto {
    private Long userId;

    private Integer readingAmount;
    private Integer importantFactor;

    private List<String> preferredGenres;
    private List<String> preferredKeywords;
    private List<String> favoriteBookIsbns;
}

package qnb.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class UserPreferenceRequestDto {
    @NotNull
    private Integer readingAmount; //독서량,단일선택

    @NotNull
    private Integer importantFactor; //중요요소,단일선택

    @NotNull
    @Size(min = 1)
    private List<String> preferredGenres; //선호장르,복수선택

    private List<String> preferredKeywords;//키워드, 복수선택

    @JsonProperty("favoriteBooks")
    private List<Integer> preferredBookId; //좋았던 책, 복수선택
}

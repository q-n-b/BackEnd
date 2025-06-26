package qnb.book.dto;

import lombok.Getter;

@Getter
public class BookScrapRequestDto {
    private String status; // "WISH", "READING", "READ"
}

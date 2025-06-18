package qnb.common.dto;

//UI에서 보여지는 화면 페이지 정보

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageInfoDto {
    private int currentPage;
    private int totalPages;
    private long totalElements;
}

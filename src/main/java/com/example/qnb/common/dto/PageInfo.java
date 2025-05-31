package com.example.qnb.common.dto;

//UI에서 보여지는 화면 페이지 정보

import lombok.Data;

@Data
public class PageInfo {

    private int currentPage; //현재 응답된 페이지 번호
    private int totalPages; //전체 페이지수
    private long totalElements; //전체 질문 개수

    public PageInfo(int currentPage, int totalPages, long totalElements) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}

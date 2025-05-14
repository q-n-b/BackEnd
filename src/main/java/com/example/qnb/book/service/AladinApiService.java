package com.example.qnb.book.service;
//알라딘 API 호출 서비스

import com.example.qnb.book.entity.Book;
import com.example.qnb.book.repository.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AladinApiService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${aladin.ttbkey}")
    private String ttbKey;

    public AladinApiService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void fetchBooksByCategory(String genre, int categoryId) {
        int page = 1;
        int totalSaved = 0;

        while (true) {
            String url = String.format(
                    "http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=%s&QueryType=Bestseller&MaxResults=100&start=%d&SearchTarget=Book&output=js&CategoryId=%d&Version=20131101",
                    ttbKey, page, categoryId
            );

            //디버깅용 코드
            //System.out.println("▶ API 호출 URL: " + url);

            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                //✅ [2단계] API 응답 본문 로그 출력
                System.out.println("▶ API 응답 내용:");
                System.out.println(response.getBody());

                List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("item");

                if (items == null || items.isEmpty()) {
                    System.out.printf(" [%s] 카테고리 수집 완료 (총 %d권 저장됨)\n", genre, totalSaved);
                    break;
                }

                for (Map<String, Object> item : items) {
                    String isbn13 = (String) item.get("isbn13");

                    // 중복 체크
                    if (isbn13 == null || bookRepository.existsByIsbn13(isbn13)) continue;

                    Book book = new Book();
                    book.setTitle((String) item.get("title"));
                    book.setAuthor((String) item.get("author"));
                    book.setGenre(genre);
                    book.setPublisher((String) item.get("publisher"));
                    book.setImageUrl((String) item.get("cover"));
                    book.setIsbn13(isbn13);
                    book.setDescription((String) item.get("description"));

                    // 출판 연도 추출
                    String pubDate = (String) item.get("pubDate");
                    if (pubDate != null && pubDate.length() >= 4) {
                        try {
                            int year = Integer.parseInt(pubDate.substring(0, 4));
                            book.setPublishedYear(year);
                        } catch (NumberFormatException ignored) {}
                    }

                    bookRepository.save(book);
                    totalSaved++;
                }

                page++; // 다음 페이지로

            } catch (Exception e) {
                System.err.println("API 요청 실패 (page " + page + "): " + e.getMessage());
                break;
            }

        }
    }
}
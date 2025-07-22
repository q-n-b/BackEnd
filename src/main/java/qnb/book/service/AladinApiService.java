package qnb.book.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import qnb.book.dto.AladinBookXml;
import qnb.book.dto.AladinResponseXml;
import qnb.book.dto.Category;
import qnb.book.entity.Book;
import qnb.book.repository.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AladinApiService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${aladin.ttbkey}")
    private String ttbKey;

    public AladinApiService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void fetchAllCategories() {
        List<Category> categories = List.of(
                new Category("한국소설", 50993),
                new Category("과학소설", 89505),
                new Category("로맨스소설", 103706),
                new Category("자기계발", 70216),
                new Category("에세이", 50760)
        );

        for (Category category : categories) {
            fetchBooksByCategory(category.getName(), category.getId());
        }
    }

    public void fetchBooksByCategory(String genre, int categoryId) {
        int page = 1;
        int totalSaved = 0;
        XmlMapper xmlMapper = new XmlMapper();

        while (page <= 4) {
            int start = (page - 1) * 50 + 1;
            
            String url = String.format(
                    "http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=%s&QueryType=ItemNewAll&MaxResults=50&start=%d&SearchTarget=Book&output=xml&CategoryId=%d&Version=20131101",
                    ttbKey, start, categoryId
            );

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                String xmlBody = response.getBody();

                AladinResponseXml parsed = xmlMapper.readValue(xmlBody, AladinResponseXml.class);
                List<AladinBookXml> items = parsed.getItems();

                if (items == null || items.isEmpty()) {
                    System.out.printf(" [%s] 카테고리 수집 완료 (총 %d권 저장됨)\n", genre, totalSaved);
                    break;
                }

                for (AladinBookXml item : items) {
                    //System.out.println("[DEBUG] title = " + item.getTitle() + ", isbn13 = " + item.getIsbn13());

                    String isbn13 = item.getIsbn13();
                    if (isbn13 == null || bookRepository.existsByIsbn13(isbn13)) continue;

                    Book book = new Book();
                    book.setTitle(item.getTitle());
                    book.setAuthor(item.getAuthor());
                    book.setGenre(genre);
                    book.setPublisher(item.getPublisher());
                    book.setImageUrl(item.getCover());
                    book.setIsbn13(isbn13);
                    book.setDescription(item.getDescription());

                    String pubDate = item.getPubDate();
                    if (pubDate != null && pubDate.length() >= 4) {
                        try {
                            int year = Integer.parseInt(pubDate.substring(0, 4));
                            book.setPublishedYear(year);
                        } catch (NumberFormatException ignored) {}
                    }

                    book.setSalesPoint(item.getSalesPoint());

                    bookRepository.save(book);
                    totalSaved++;
                }

                page++;

            } catch (Exception e) {
                System.err.printf("API 요청 실패 (%s, page %d): %s\n", genre, page, e.getMessage());
                break;
            }
        }
    }
}
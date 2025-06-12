package qnb.book.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookId; //내가 부여하는 bookId(기본키)

    @Column(unique = true) //고유값 설정
    private String isbn13; //책마다 부여되는 isbn (중복 데이터 방지 기능)

    @Column(length = 255)
    private String title; //제목

    @Column(length = 255)
    private String author; //작가

    @Column(length = 255)
    private String genre; //장르(카테고리)

    @Column(name = "published_year")
    private Integer publishedYear; //발행연도

    @Column(name = "image_url", length = 255)
    private String imageUrl; //표지 Url

    @Column(length = 255)
    private String publisher; //출판사

    @Column(columnDefinition = "TEXT")
    private String description; //책 설명
}
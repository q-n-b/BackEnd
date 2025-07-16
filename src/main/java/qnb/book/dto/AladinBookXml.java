package qnb.book.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AladinBookXml {
    private String title;
    private String author;
    private String publisher;
    private String isbn13;
    private String pubDate;
    private String description;
    private String cover;
    private Integer salesPoint;

    @JacksonXmlProperty(localName = "categoryName")
    private String categoryName;

}

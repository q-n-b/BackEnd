package qnb.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qnb.common.exception.InvalidSearchModeException;
import qnb.common.exception.MissingFieldException;
import qnb.common.exception.SearchNoResultException;
import qnb.search.service.SearchService;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam String mode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);

        if (mode.equals("summary")) {
            if (keyword == null || keyword.trim().isEmpty()) {
                throw new SearchNoResultException();
            }
            return ResponseEntity.ok(searchService.searchSummary(keyword));
        }

        if (mode.equals("full")) {
            if (type == null || (!type.equals("BOOK") && !type.equals("QUESTION") && !type.equals("ANSWER"))) {
                throw new MissingFieldException();
            }
            return ResponseEntity.ok(
                    searchService.searchFull(type, keyword, safePage, safeSize, sort)
            );
        }

        throw new InvalidSearchModeException();
    }
}


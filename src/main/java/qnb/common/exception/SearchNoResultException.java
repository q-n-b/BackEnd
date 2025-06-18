package qnb.common.exception;

public class SearchNoResultException extends RuntimeException {
    public SearchNoResultException() {
        super("검색 결과가 없습니다.");
    }
}

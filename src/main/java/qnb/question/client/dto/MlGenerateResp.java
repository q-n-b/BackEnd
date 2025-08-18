package qnb.question.client.dto;

public record MlGenerateResp(
        Long bookId,
        boolean blocked,
        String question,   // blocked=false 일 때만 유효
        Integer latencyMs, // optional
        String reason      // blocked=true 일 때 optional
) {}

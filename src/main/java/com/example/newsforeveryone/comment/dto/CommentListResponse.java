package com.example.newsforeveryone.comment.dto;

import java.util.List;

public record CommentListResponse(
    List<CommentResponse> content,
    String nextCursor,
    String nextAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) { }

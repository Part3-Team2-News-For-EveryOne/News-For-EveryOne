package com.example.newsforeveryone.comment.dto;

import java.util.List;

public record CommentListResponse(
    List<CommentResponse> comments,
    String nextCursor,
    Boolean hasNext
) { }

package com.example.newsforeveryone.notification.entity;

public enum ResourceType {

    INTEREST("%s과(와) 관련된 기사가 %d건 등록되었습니다."),
    COMMENT("%s님이 나의 댓글을 좋아합니다.");
    private final String format;

    ResourceType(String format) {
        this.format = format;
    }

    public static String ofInterestContent(String interestName, Long count) {
        return String.format(ResourceType.INTEREST.format, interestName, count);
    }

    public static String ofCommentContent(String likerName) {
        return String.format(ResourceType.COMMENT.format, likerName);
    }

}

package com.example.newsforeveryone.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    // 사용자 관련
    INVALID_CREDENTIALS(401, "U01", "이메일 또는 비밀번호가 올바르지 않습니다"),
    UNAUTHORIZED_USER_ACCESS(403, "U02", "해당 사용자에 대한 권한이 없습니다"),
    USER_NOT_FOUND(404, "U03", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(409, "U04", "이미 존재하는 이메일입니다"),

    // 관심사 관련
    INTEREST_NOT_FOUND(404, "I1", "관심사를 찾을 수 없습니다."),
    INTEREST_AlREADY_EXIST(404, "I2", "유사한 관심사가 이미 존재합니다."),

    // 댓글 관련
    COMMENT_UPDATE_FORBIDDEN(403, "CM05", "댓글 수정 권한이 없습니다."),
    COMMENT_DELETE_FORBIDDEN(403, "CM06", "댓글 삭제 권한이 없습니다."),
    COMMENT_NOT_FOUND(404, "CM01", "댓글이 존재하지 않습니다."),
    COMMENT_LIKE_NOT_FOUND(404, "CM04", "좋아요 정보가 존재하지 않습니다."),
    COMMENT_LIKE_DUPLICATED(409, "CM03", "이미 좋아요를 누른 댓글입니다."),
    COMMENT_ALREADY_DELETED(410, "CM02", "이미 삭제된 댓글입니다."),

    // 기사 관련
    ARTICLE_NOT_FOUND(404,"A03", "기사를 찾을 수 없습니다"),
    // 공통
    INVALID_REQUEST(400, "C01", "잘못된 요청입니다"),
    INTERNAL_SERVER_ERROR(500, "C02", "서버 내부 오류가 발생했습니다");

    private final int httpStatus;
    private final String code;
    private final String message;

}

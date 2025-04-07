package hekireki.sanjijiksong.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //Member
    USER_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 존재하는 회원입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    USER_EMAIL_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    USER_ALREADY_DEACTIVATED(HttpStatus.BAD_REQUEST, "이미 탈퇴된 사용자입니다."),
    USER_ALREADY_RESTORED(HttpStatus.BAD_REQUEST, "이미 복구된 사용자입니다."),
    USER_RESTORE_EXPIRED(HttpStatus.BAD_REQUEST, "복구 가능 기간이 만료되었습니다."),
    USER_UNAUTHORIZED(HttpStatus.FORBIDDEN, "본인만 할 수 있습니다."),

    // Store
    STORE_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 등록된 가게가 존재합니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 가게입니다."),


    //Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),

    // KAMIS API 호출 에러
    KAMIS_API_NO_DATA(HttpStatus.BAD_REQUEST, "KAMIS API에서 데이터가 없습니다."),
    KAMIS_API_WRONG_PARAMETER(HttpStatus.BAD_REQUEST, "KAMIS API에서 잘못된 파라미터입니다."),
    KAMIS_API_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "KAMIS API 인증에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}

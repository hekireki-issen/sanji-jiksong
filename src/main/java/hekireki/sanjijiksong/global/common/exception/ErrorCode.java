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
    UNAUTHORIZED_STORE_OWNER(HttpStatus.FORBIDDEN, "해당 가게에 대한 소유권이 없습니다."),
    STORE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 가게에 대한 권한이 없습니다."),
    STORE_ALREADY_DEACTIVATED(HttpStatus.BAD_REQUEST, "이미 비활성화된 가게입니다."),


    //Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    ORDER_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "취소할 수 없는 주문 상태입니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 항목이 존재하지 않습니다."),
    ORDER_NOT_UPDATABLE(HttpStatus.BAD_REQUEST, "현재 상태에서는 주문을 수정할 수 없습니다."),

    //Security
    NO_REFRESH_TOKEN_COOKIE(HttpStatus.BAD_REQUEST, "리프레시 토큰 쿠키가 존재하지 않습니다."),
    MISSING_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 누락되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "DB에 해당 리프레시 토큰이 존재하지 않습니다."),

    // KAMIS API 호출 에러
    KAMIS_API_NO_DATA(HttpStatus.BAD_REQUEST, "KAMIS API에서 데이터가 없습니다."),
    KAMIS_API_WRONG_PARAMETER(HttpStatus.BAD_REQUEST, "KAMIS API에서 잘못된 파라미터입니다."),
    KAMIS_API_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "KAMIS API 인증에 실패했습니다."),

    //Item
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "상품이 존재하지 않습니다"),
    ITEM_ALREADY_DEACTIVATED(HttpStatus.BAD_REQUEST, "이미 비활성화된 상품입니다."),
    ITEM_STOCK_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "상품 재고가 부족합니다.");

    private final HttpStatus status;
    private final String message;
}

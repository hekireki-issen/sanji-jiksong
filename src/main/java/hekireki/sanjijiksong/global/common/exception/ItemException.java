package hekireki.sanjijiksong.global.common.exception;

import lombok.Getter;

@Getter
public class ItemException extends RuntimeException {

    private final ErrorCode errorCode;

    public ItemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

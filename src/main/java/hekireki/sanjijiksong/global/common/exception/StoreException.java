package hekireki.sanjijiksong.global.common.exception;

import lombok.Getter;

@Getter
public class StoreException extends RuntimeException {

    private final ErrorCode errorCode;

    public StoreException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

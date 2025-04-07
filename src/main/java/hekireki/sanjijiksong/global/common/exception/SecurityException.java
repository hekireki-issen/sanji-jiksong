package hekireki.sanjijiksong.global.common.exception;

import lombok.Getter;

@Getter
public class SecurityException extends RuntimeException {

    private final ErrorCode errorCode;

    public SecurityException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

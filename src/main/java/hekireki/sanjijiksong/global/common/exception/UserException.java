package hekireki.sanjijiksong.global.common.exception;

import lombok.Getter;

@Getter
public class UserException extends RuntimeException{
    private final ErrorCode errorCode;

    public UserException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }


    public static class UserAlreadyExistsException extends UserException {
        public UserAlreadyExistsException() {
            super(ErrorCode.USER_ALREADY_EXIST);
        }
    }

    public static class UserNotFoundException extends UserException {
        public UserNotFoundException() {
            super(ErrorCode.USER_NOT_FOUND);
        }
    }
}

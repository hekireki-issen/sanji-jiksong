package hekireki.sanjijiksong.global.common.exception;

import lombok.Getter;

@Getter
public class KamisException extends RuntimeException {

    private final ErrorCode errorCode;

    public KamisException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static class KamisApiNoDataException extends KamisException {
        public KamisApiNoDataException() {
            super(ErrorCode.KAMIS_API_NO_DATA);
        }
    }

    public static class KamisApiWrongParameterException extends KamisException {
        public KamisApiWrongParameterException() {
            super(ErrorCode.KAMIS_API_WRONG_PARAMETER);
        }
    }

    public static class KamisApiUnauthenticatedException extends KamisException {
        public KamisApiUnauthenticatedException() {
            super(ErrorCode.KAMIS_API_UNAUTHENTICATED);
        }
    }

    // ----------- 가격 조회 관련 예외 -------------
    public static class PriceQueryPeriodTooLongException extends KamisException {
        public PriceQueryPeriodTooLongException() {
            super(ErrorCode.PriceQueryPeriodTooLong);
        }
    }

}

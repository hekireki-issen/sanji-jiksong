package hekireki.sanjijiksong.global.common.exception;

import lombok.Getter;

@Getter
public class OrderException extends RuntimeException {

    private final ErrorCode errorCode;

    public OrderException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static class OrderNotFoundException extends OrderException {
      public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
      }
    }

    public static class OrderNotCancelableException extends OrderException {
      public OrderNotCancelableException() {
        super(ErrorCode.ORDER_NOT_CANCELABLE);
      }
    }

    public static class OrderItemNotFoundException extends OrderException {
        public OrderItemNotFoundException() {
            super(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }
    }

    public static class OrderNotUpdatableException extends OrderException {
        public OrderNotUpdatableException() {
            super(ErrorCode.ORDER_NOT_UPDATABLE);
        }
    }

}

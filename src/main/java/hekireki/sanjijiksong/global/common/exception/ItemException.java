package hekireki.sanjijiksong.global.common.exception;

import hekireki.sanjijiksong.domain.item.entity.Item;
import lombok.Getter;

@Getter
public class ItemException extends RuntimeException {

    private final ErrorCode errorCode;

    public ItemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static class ItemNotFoundException extends ItemException {
        public ItemNotFoundException() {
            super(ErrorCode.ITEM_NOT_FOUND);
        }
    }
}

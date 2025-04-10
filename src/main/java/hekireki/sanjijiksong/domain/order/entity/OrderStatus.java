package hekireki.sanjijiksong.domain.order.entity;

public enum OrderStatus {
    ORDERED,     // 주문 완료
    PAID,        // 결제 완료
    SHIPPING,    // 배송 중
    DELIVERED,    // 배송 완료
    CANCELED     // 주문자에 의한 취소
}

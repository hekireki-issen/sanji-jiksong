package hekireki.sanjijiksong.domain.order.controller;

import hekireki.sanjijiksong.domain.order.dto.OrderListUpdateRequest;
import hekireki.sanjijiksong.domain.order.dto.OrderRequest;
import hekireki.sanjijiksong.domain.order.dto.OrderResponse;
import hekireki.sanjijiksong.domain.order.service.OrderService;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUYER')")
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid OrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        OrderResponse response = orderService.createOrder(userDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    // 주문 취소
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        orderService.cancelOrder(orderId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    // 주문 수정
    @PatchMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<OrderResponse> updateOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestBody @Valid OrderListUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        OrderResponse response = orderService.updateOrderItem(
                orderId, itemId, request.count(), userDetails.getUser()
        );
        return ResponseEntity.ok(response);
    }


    // 단일 주문 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        OrderResponse response = orderService.getOrderDetail(orderId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    // 내 주문 전체 조회
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<OrderResponse> orders = orderService.getMyOrders(userDetails.getUser());
        return ResponseEntity.ok(orders);
    }
}

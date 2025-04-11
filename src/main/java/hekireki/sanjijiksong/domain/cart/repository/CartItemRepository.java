package hekireki.sanjijiksong.domain.cart.repository;

import hekireki.sanjijiksong.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 특정 장바구니 안의 모든 CartItem 조회
    List<CartItem> findByCartId(Long cartId);

    // 특정 장바구니 안의 특정 상품이 담긴 CartItem 조회
    Optional<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);

    // 카트 아이템을 모두 삭제 (예: 유저 주문 완료 시)
    void deleteByCartId(Long cartId);
}
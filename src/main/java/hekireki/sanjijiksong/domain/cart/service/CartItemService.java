package hekireki.sanjijiksong.domain.cart.service;

import hekireki.sanjijiksong.domain.cart.entity.Cart;
import hekireki.sanjijiksong.domain.cart.entity.CartItem;
import hekireki.sanjijiksong.domain.cart.repository.CartItemRepository;
import hekireki.sanjijiksong.domain.cart.repository.CartRepository;
import hekireki.sanjijiksong.domain.cart.dto.CartItemResponseDto;
import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import hekireki.sanjijiksong.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public CartItemResponseDto addCartItem(Long cartId, Long itemId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("장바구니가 존재하지 않습니다."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));

        CartItem cartItem = CartItem.create(cart, item, quantity);
        cartItemRepository.save(cartItem);

        return new CartItemResponseDto(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getImage(),
                cartItem.getQuantity(),
                cartItem.getTotalPrice(),
                item.getItemStatus().name()
        );
    }

    public List<CartItemResponseDto> getCartItems(Long cartId) {
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        return cartItems.stream()
                .map(CartItemResponseDto::from)
                .toList();
    }

    //장바구니 상품 추가 또는 수량 증가
    @Transactional
    public void addOrUpdateCartItem(Long userId, Long itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        if (item.getItemStatus() == ItemStatus.SOLDOUT) {
            throw new IllegalStateException("품절된 상품은 장바구니에 담을 수 없습니다.");
        }

        // 이미 장바구니에 있는 상품인지 확인
        CartItem existingItem = cart.getCartItems().stream()
                .filter(ci -> ci.getItem().getId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.create(cart, item, quantity);
            cartItemRepository.save(newItem);
        }
    }


    //장바구니 상품 수량 변경
    //수량이 0이면 해당 상품 삭제
    @Transactional
    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 상품을 찾을 수 없습니다."));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.updateQuantity(quantity);
        }
    }

    //장바구니에서 특정 상품 제거

    @Transactional
    public void removeCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 상품을 찾을 수 없습니다."));

        cartItemRepository.delete(cartItem);
    }

    //장바구니 전체 비우기
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다."));

        cartItemRepository.deleteAll(cart.getCartItems());
    }
}
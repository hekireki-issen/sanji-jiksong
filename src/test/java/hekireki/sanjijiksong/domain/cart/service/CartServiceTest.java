package hekireki.sanjijiksong.domain.cart.service;

import hekireki.sanjijiksong.domain.cart.dto.CartResponseDto;
import hekireki.sanjijiksong.domain.cart.entity.Cart;
import hekireki.sanjijiksong.domain.cart.entity.CartItem;
import hekireki.sanjijiksong.domain.cart.repository.CartRepository;
import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import hekireki.sanjijiksong.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    private CartRepository cartRepository;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        cartService = new CartService(cartRepository);
    }

    @Test
    void 장바구니_조회_성공() {
        // given
        Long userId = 1L;

        Item item = Item.builder()
                .id(10L)
                .name("테스트 상품")
                .price(2000)
                .image("img.jpg")
                .stock(10)
                .itemStatus(ItemStatus.ONSALE)
                .build();

        Cart cart = new Cart(); // @OneToOne User 등은 생략 가능
        CartItem cartItem = CartItem.create(cart, item, 3); // 수량 3
        cart.getCartItems().add(cartItem);

        when(cartRepository.findByUser_Id(userId)).thenReturn(Optional.of(cart));

        // when
        CartResponseDto response = cartService.getCartByUserId(userId);

        // then
        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(cart.getId(), response.cartId());
        assertEquals(1, response.items().size());

        var dto = response.items().get(0);
        assertEquals(10L, dto.itemId());
        assertEquals("테스트 상품", dto.itemName());
        assertEquals(3, dto.quantity());
        assertEquals(6000, response.totalPrice());

        assertEquals(3, cartItem.getQuantity());
        assertEquals(6000, cartItem.getTotalPrice());
    }

    @Test
    void 장바구니_없을_때_예외발생() {
        // given
        Long userId = 1L;
        when(cartRepository.findByUser_Id(userId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            cartService.getCartByUserId(userId);
        });

        assertEquals("장바구니가 존재하지 않습니다.", e.getMessage());
    }

}

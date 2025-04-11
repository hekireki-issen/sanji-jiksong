package hekireki.sanjijiksong;

import hekireki.sanjijiksong.domain.cart.dto.CartItemResponseDto;
import hekireki.sanjijiksong.domain.cart.entity.Cart;
import hekireki.sanjijiksong.domain.cart.service.CartItemService;
import hekireki.sanjijiksong.domain.cart.repository.CartItemRepository;
import hekireki.sanjijiksong.domain.cart.repository.CartRepository;
import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.repository.ItemRepository;
import java.util.List;
import hekireki.sanjijiksong.domain.cart.entity.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CartItemServiceTest {

    private CartRepository cartRepository;
    private ItemRepository itemRepository;
    private CartItemRepository cartItemRepository;
    private CartItemService cartItemService;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        itemRepository = mock(ItemRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        cartItemService = new CartItemService(cartRepository, cartItemRepository, itemRepository);
    }

    @Test
    void 장바구니에_아이템_추가_성공() {
        // given
        Long cartId = 1L;
        Long itemId = 2L;
        int quantity = 3;

        Cart cart = new Cart();
        Item item = Item.builder()
                .id(itemId)
                .price(1000)
                .stock(10)
                .name("테스트상품")
                .image("test.jpg")
                .itemStatus(hekireki.sanjijiksong.domain.item.entity.ItemStatus.ONSALE)
                .build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // when
        CartItemResponseDto response = cartItemService.addCartItem(cartId, itemId, quantity);

        // then
        assertNotNull(response);
        assertEquals(itemId, response.itemId());
        assertEquals(quantity, response.quantity());
        assertEquals("테스트상품", response.itemName());
        assertEquals(3000, response.totalPrice()); // 1000 * 3
    }

    @Test
    void 장바구니_전체_비우기_성공() {
        // given
        Long userId = 1L;
        Cart cart = new Cart();
        List<CartItem> cartItems = List.of(
                CartItem.create(cart, mock(Item.class), 2),
                CartItem.create(cart, mock(Item.class), 3)
        );
        cart.getCartItems().addAll(cartItems);

        when(cartRepository.findByUser_Id(userId)).thenReturn(Optional.of(cart));

        // when
        cartItemService.clearCart(userId);

        // then
        verify(cartItemRepository).deleteAll(cartItems); // 삭제가 호출되었는지 확인
    }

    @Test
    void 수량이_0이면_장바구니_아이템_삭제() {
        // given
        Long cartItemId = 1L;
        CartItem cartItem = mock(CartItem.class);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // when
        cartItemService.updateCartItemQuantity(cartItemId, 0);

        // then
        verify(cartItemRepository).delete(cartItem);
        verify(cartItem, never()).updateQuantity(anyInt());
    }

    @Test
    void 수량이_1이상이면_수량_업데이트() {
        // given
        Long cartItemId = 1L;
        int newQuantity = 5;
        CartItem cartItem = mock(CartItem.class);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // when
        cartItemService.updateCartItemQuantity(cartItemId, newQuantity);

        // then
        verify(cartItem).updateQuantity(newQuantity);
        verify(cartItemRepository, never()).delete(any());
    }
}
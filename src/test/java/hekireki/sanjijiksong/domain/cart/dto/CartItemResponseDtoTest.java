package hekireki.sanjijiksong.domain.cart.dto;

import org.junit.jupiter.api.Test;
import hekireki.sanjijiksong.domain.cart.entity.Cart;
import hekireki.sanjijiksong.domain.cart.entity.CartItem;
import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CartItemResponseDtoTest {


    @Test
    void 카트아이템으로부터_DTO_생성_성공() {
        // given
        Item item = Item.builder()
                .id(1L)
                .name("테스트상품")
                .price(2000)
                .image("img.jpg")
                .itemStatus(ItemStatus.ONSALE)
                .stock(10)
                .build();

        Cart cart = new Cart();
        CartItem cartItem = CartItem.create(cart, item, 3);

        // when
        CartItemResponseDto dto = CartItemResponseDto.from(cartItem);

        // then
        assertEquals(Long.valueOf(1L), dto.itemId()); // ← 여기 수정
        assertEquals("테스트상품", dto.itemName());
        assertEquals(2000, dto.price());
        assertEquals("img.jpg", dto.image());
        assertEquals(3, dto.quantity());
        assertEquals(6000, dto.totalPrice());
        assertEquals("ONSALE", dto.itemStatus());
    }

    @Test
    void 모든_필드_정상적으로_세팅되는지_확인() {
        CartItemResponseDto dto = new CartItemResponseDto(
                1L,
                "테스트상품",
                2000,
                "img.jpg",
                3,
                6000,
                "ONSALE"
        );

        assertEquals(1L, dto.itemId());
        assertEquals("테스트상품", dto.itemName());
        assertEquals(2000, dto.price());
        assertEquals("img.jpg", dto.image());
        assertEquals(3, dto.quantity());
        assertEquals(6000, dto.totalPrice());
        assertEquals("ONSALE", dto.itemStatus());
    }

    @Test
    void dto_동등성_테스트() {
        CartItemResponseDto dto1 = new CartItemResponseDto(1L, "상품", 1000, "img.jpg", 2, 2000, "ONSALE");
        CartItemResponseDto dto2 = new CartItemResponseDto(1L, "상품", 1000, "img.jpg", 2, 2000, "ONSALE");

        assertEquals(dto1, dto2); // equals()
        assertEquals(dto1.hashCode(), dto2.hashCode()); // hashCode()
        assertTrue(dto1.toString().contains("상품")); // toString()
    }


}

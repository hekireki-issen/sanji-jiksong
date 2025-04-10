package hekireki.sanjijiksong.domain.cart.service;

import hekireki.sanjijiksong.domain.cart.dto.CartResponseDto;
import hekireki.sanjijiksong.domain.cart.dto.CartItemResponseDto;
import hekireki.sanjijiksong.domain.cart.entity.Cart;
import hekireki.sanjijiksong.domain.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class CartService {

    private final CartRepository cartRepositroy;

    public CartResponseDto getCartByUserId(Long userId){

        // 장바구니 에러 확인
        // System.out.println("userId: " + userId);
        //유저 장바구니 읽기
        Cart cart = cartRepositroy.findByUser_Id(userId).orElseThrow(() -> new RuntimeException("장바구니가 존재하지 않습니다."));

        // 에러확인
        //System.out.println("cartId: " + cart.getId());
        // System.out.println("장바구니 안 항목 수: " + cart.getCartItems().size());

        //장바구니 안의 상품 목록 읽기 + DTO로 변환
        List<CartItemResponseDto> itemDtos = cart.getCartItems().stream()
                .map(cartItem -> new CartItemResponseDto(
                        cartItem.getItem().getId(),
                        cartItem.getItem().getName(),
                        cartItem.getItem().getPrice(),
                        cartItem.getItem().getImage(),
                        cartItem.getQuantity(),
                        cartItem.getTotalPrice(),               // 추가
                        cartItem.getItem().getItemStatus().name()
                ))
                .collect(Collectors.toList());

        //총 가격 계산
        int totalPrice = itemDtos.stream()
                .mapToInt(item -> item.price() * item.quantity())
                .sum();

        //결과를 DTO로 묶어서 리턴
        return new CartResponseDto(
                cart.getId(),
                userId,
                itemDtos,
                totalPrice
        );
    }
}

package hekireki.sanjijiksong.domain.order.api;

import hekireki.sanjijiksong.domain.user.dto.UserRegisterRequest;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import hekireki.sanjijiksong.domain.order.dto.OrderRequest;
import hekireki.sanjijiksong.domain.order.dto.OrderResponse;
import hekireki.sanjijiksong.domain.order.dto.OrderListUpdateRequest;

import java.util.List;

@Tag(name = "OrderController", description = "주문 Controller")
@RequestMapping("/api/v1/orders")
public interface OrderApi {

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "주문 생성 성공", content = @Content(schema = @Schema(implementation = OrderResponse.class), examples = @ExampleObject(name = "주문 생성 예시", value = """
                    {
                      "orderId": 1,
                      "orderStatus": "ORDERED",
                      "totalCount": 2,
                      "totalPrice": 20000,
                      "orderLists": [
                        {
                          "itemId": 1,
                          "itemName": "상품명",
                          "price": 10000,
                          "count": 2,
                          "totalPrice": 20000,
                          "storeId": 1,
                          "storeName": "가게"
                        }
                      ]
                    }
            """))), @ApiResponse(responseCode = "400", description = "요청 오류", content = @Content(examples = @ExampleObject(name = "잘못된 요청 예시", value = """
                {
                  "code": "INVALID_ARGUMENT",
                  "message": "입력값이 유효하지 않습니다."
                }
            """)))})
    @PostMapping
    ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(
            summary = "주문 취소",
            description = "주문을 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "주문 취소 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 취소된 주문이거나 취소할 수 없는 상태",
                    content = @Content(
                            examples = @ExampleObject(name = "취소 불가 상태 예시", value = """
                {
                  "code": "ORDER_ALREADY_CANCELED",
                  "message": "이미 취소된 주문입니다."
                }
            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문 없음",
                    content = @Content(
                            examples = @ExampleObject(name = "주문 없음 예시", value = """
                {
                  "code": "ORDER_NOT_FOUND",
                  "message": "주문을 찾을 수 없습니다."
                }
            """)
                    )
            )
    })
    @PatchMapping("/{orderId}/cancel")
    ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );


    @Operation(
            summary = "주문 수정",
            description = "주문 내 항목 수량을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(name = "수정 성공 예시", value = """
                                        {
                                          "orderId": 1,
                                          "orderStatus": "ORDERED",
                                          "totalCount": 3,
                                          "totalPrice": 30000,
                                          "orderLists": [
                                            {
                                              "itemId": 1,
                                              "itemName": "상품1",
                                              "price": 10000,
                                              "count": 3,
                                              "totalPrice": 30000,
                                              "storeId": 1,
                                              "storeName": "가게1"
                                            }
                                          ]
                                        }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "수정 불가 상태",
                    content = @Content(
                            examples = @ExampleObject(name = "수정 불가 예시", value = """
                                        {
                                          "code": "ORDER_NOT_UPDATABLE",
                                          "message": "주문 상태에서는 수정할 수 없습니다."
                                        }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문 또는 항목 없음",
                    content = @Content(
                            examples = @ExampleObject(name = "주문 또는 항목 없음 예시", value = """
                                        {
                                          "code": "ORDER_ITEM_NOT_FOUND",
                                          "message": "해당 주문 항목을 찾을 수 없습니다."
                                        }
                                    """)
                    )
            )
    })
    @PatchMapping("/{orderId}/items/{itemId}")
    ResponseEntity<OrderResponse> updateOrderItems(
            @PathVariable Long orderId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 주문 항목 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderListUpdateRequest.class),
                            examples = @ExampleObject(value = """
                                        {
                                          "orderId": 1,
                                          "orderLists": [
                                            {
                                            "itemId": 1,
                                            "count": 3
                                            }
                                          ]
                                        }
                                    """))
            ) OrderListUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );


    @Operation(
            summary = "단일 주문 조회",
            description = "특정 주문의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(name = "조회 성공 예시", value = """
                                        {
                                          "orderId": 1,
                                          "orderStatus": "ORDERED",
                                          "totalCount": 2,
                                          "totalPrice": 20000,
                                          "orderLists": [
                                            {
                                              "itemId": 1,
                                              "itemName": "상품1",
                                              "price": 10000,
                                              "count": 2,
                                              "totalPrice": 20000,
                                              "storeId": 1,
                                              "storeName": "가게1"
                                            }
                                          ]
                                        }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문 없음",
                    content = @Content(
                            examples = @ExampleObject(name = "주문 없음 예시", value = """
                                        {
                                          "code": "ORDER_NOT_FOUND",
                                          "message": "주문을 찾을 수 없습니다."
                                        }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            examples = @ExampleObject(name = "인증 실패 예시", value = """
                                        {
                                          "code": "UNAUTHORIZED",
                                          "message": "인증 정보가 유효하지 않습니다."
                                        }
                                    """)
                    )
            )
    })
    @GetMapping("/{orderId}")
    ResponseEntity<OrderResponse> getOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "내 주문 전체 조회", description = "로그인한 사용자의 모든 주문 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)),
                            examples = @ExampleObject(name = "조회 성공 예시", value = """
                                        [
                                          {
                                            "orderId": 1,
                                            "orderStatus": "ORDERED",
                                            "totalCount": 2,
                                            "totalPrice": 20000,
                                            "orderLists": [
                                              {
                                                "itemId": 1,
                                                "itemName": "상품1",
                                                "price": 10000,
                                                "count": 2,
                                                "totalPrice": 20000,
                                                "storeId": 1,
                                                "storeName": "가게1"
                                              }
                                            ]
                                          },
                                          {
                                            "orderId": 2,
                                            "orderStatus": "CANCELED",
                                            "totalCount": 1,
                                            "totalPrice": 5000,
                                            "orderLists": [
                                              {
                                                "itemId": 2,
                                                "itemName": "상품2",
                                                "price": 5000,
                                                "count": 1,
                                                "totalPrice": 5000,
                                                "storeId": 2,
                                                "storeName": "가게2"
                                              }
                                            ]
                                          }
                                        ]
                                    """))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(examples = @ExampleObject(name = "인증 실패 예시", value = """
                                {
                                  "code": "UNAUTHORIZED",
                                  "message": "인증 정보가 유효하지 않습니다."
                                }
                            """)))
    })
    @GetMapping
    ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal CustomUserDetails userDetails);

}
package hekireki.sanjijiksong.domain.item.dto;

import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import hekireki.sanjijiksong.domain.store.entity.Store;

public record ItemCreateRequest(
        //Todo : 수정 필요
        String category,
        String itemName,
        int price,
        String image,
        int stock,
        String description
) {
    public Item toEntity(Store store){
        return Item.builder()
                .store(store)
                .name(itemName)
                .price(price)
                .image(image)
                .stock(stock)
                .description(description)
                .active(true)
                .itemStatus(ItemStatus.ONSALE)
                //Todo : 수정 필요
                .category(category)
                .build();
    }
}

package hekireki.sanjijiksong.domain.item.dto;

import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.entity.Store;

public record ItemResponse(
        Long id,
        Long storeId,
        String name,
        int price,
        String image,
        int stock,
        String description,
        ItemStatus itemStatus,
        //Todo 수정필요
        String category
)
{
    public static ItemResponse of(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getStore().getId(),
                item.getName(),
                item.getPrice(),
                item.getImage(),
                item.getStock(),
                item.getDescription(),
                item.getItemStatus(),
                item.getCategory()
        );
    }
}

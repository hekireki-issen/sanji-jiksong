package hekireki.sanjijiksong.domain.item.dto;

import hekireki.sanjijiksong.domain.item.entity.ItemStatus;

public record ItemUpdateRequest(
        //Todo : 수정 필요
        String category,
        String itemName,
        Integer price,
        String image,
        Integer stock,
        String description,
        Boolean active,
        ItemStatus itemStatus
) {
}

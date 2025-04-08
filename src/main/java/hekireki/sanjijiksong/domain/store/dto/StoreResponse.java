package hekireki.sanjijiksong.domain.store.dto;

import hekireki.sanjijiksong.domain.store.entity.Store;

public record StoreResponse(
        Long id,
        String name,
        String address,
        String description,
        String image,
        Boolean active
) {
    public static StoreResponse of(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getDescription(),
                store.getImage(),
                store.getActive()
        );
    }
}

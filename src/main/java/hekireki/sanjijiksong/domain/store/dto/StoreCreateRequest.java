package hekireki.sanjijiksong.domain.store.dto;

public record StoreCreateRequest(
        String name,
        String address,
        String description,
        String image
) {
}

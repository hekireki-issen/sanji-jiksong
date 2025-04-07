package hekireki.sanjijiksong.domain.store.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StoreUpdateRequest(
        String name,
        String address,
        String description,
        String image
) {}
package hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import hekireki.sanjijiksong.domain.openapi.entity.PriceDaily;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KamisDailyResponse(
        List<Condition> condition,
        @JsonDeserialize(using = PriceDeserializer.class) Data data
) {

    public record Condition(
            @JsonProperty("p_product_cls_code") String productClsCode,
            @JsonProperty("p_country_code") List<String> countryCode,
            @JsonProperty("p_regday") String regday,
            @JsonProperty("p_convert_kg_yn") String convertKgYn,
            @JsonProperty("p_category_code") String categoryCode,
            @JsonProperty("p_cert_key") String certKey,
            @JsonProperty("p_cert_id") String certId,
            @JsonProperty("p_returntype") String returnType
    ) {}

    public record Data(
            @JsonProperty("error_code") String errorCode,
            List<Item> item
    ) {}

    public record Item(
            @JsonProperty("item_name") String itemName,
            @JsonProperty("item_code") String itemCode,
            @JsonProperty("kind_name") String kindName,
            @JsonProperty("kind_code") String kindCode,
            @JsonProperty("rank_code") String rankCode,
            String rank,
            String unit,
            String dpr1
    ) {}

    public List<PriceDaily> from() {
        if (data == null || data.item() == null) {
            throw new RuntimeException("잘못된 응답 형식입니다.");
        }

        return data.item().stream()
                .filter(item -> {
                    String normalized = safeNormalize(item.dpr1());
                    return normalized != null && !normalized.isEmpty() && !normalized.equals("0");
                })
                .map(item -> {
                    String normalized = safeNormalize(item.dpr1());
                    return PriceDaily.builder()
                            .itemName(item.itemName())
                            .itemCode(item.itemCode())
                            .kindName(item.kindName())
                            .kindCode(item.kindCode())
                            .rank(item.rank())
                            .rankCode(item.rankCode())
                            .unit(item.unit())
                            .snapshotDate(LocalDate.parse(condition().get(0).regday()))
                            .categoryCode(condition().get(0).categoryCode())
                            .classCode(condition().get(0).productClsCode())
                            .price(Integer.parseInt(normalized))
                            .build();
                })
                .toList();
    }

    private static String safeNormalize(String s) {
        if (s == null || s.trim().equals("-")) {
            return null;
        }
        return s.replaceAll(",", "").trim();
    }
}

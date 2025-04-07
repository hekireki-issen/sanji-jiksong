package hekireki.sanjijiksong.domain.price.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import hekireki.sanjijiksong.domain.price.entity.PriceDaily;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KamisDailyResponse {
    private List<Condition> condition;

    @JsonDeserialize(using = PriceDeserializer.class)
    private Data data;


    @Getter
    public static class Condition {
        @JsonProperty("p_product_cls_code")
        private String productClsCode;

        @JsonProperty("p_country_code")
        private List<String> countryCode;

        @JsonProperty("p_regday")
        private String regday;

        @JsonProperty("p_convert_kg_yn")
        private String convertKgYn;

        @JsonProperty("p_category_code")
        private String categoryCode;

        @JsonProperty("p_cert_key")
        private String certKey;

        @JsonProperty("p_cert_id")
        private String certId;

        @JsonProperty("p_returntype")
        private String returnType;
    }

    @Getter
    public static class Data {
        @JsonProperty("error_code")
        private String errorCode;

        private List<Item> item;
    }

    @Getter
    public static class Item {
        @JsonProperty("item_name")
        private String itemName;

        @JsonProperty("item_code")
        private String itemCode;

        @JsonProperty("kind_name")
        private String kindName;

        @JsonProperty("kind_code")
        private String kindCode;

        private String rank;

        @JsonProperty("rank_code")
        private String rankCode;

        private String unit;
        private String dpr1;
    }

    public List<PriceDaily> from() {
        if(data==null || data.getItem() == null) {
            throw new RuntimeException("잘못된 응답 형식입니다.");
        }

        return data.getItem().stream()
                .filter(item -> {
                    String dpr1 = safeNormalize(item.getDpr1());
                    return dpr1 != null && !dpr1.isEmpty() && !dpr1.equals("0");
                })
                .map(item -> {
                    String dpr1 = safeNormalize(item.getDpr1());

                    return PriceDaily.builder()
                            .itemName(item.getItemName())
                            .itemCode(item.getItemCode())
                            .kindName(item.getKindName())
                            .kindCode(item.getKindCode())
                            .rank(item.getRank())
                            .rankCode(item.getRankCode())
                            .unit(item.getUnit())
                            .snapshotDate(LocalDate.parse(condition.get(0).regday)) // TODO: 실제 연도 포함 날짜로 변경
                            .categoryCode(condition.get(0).categoryCode)
                            .classCode(condition.get(0).productClsCode)
                            .price(Integer.parseInt(dpr1))
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


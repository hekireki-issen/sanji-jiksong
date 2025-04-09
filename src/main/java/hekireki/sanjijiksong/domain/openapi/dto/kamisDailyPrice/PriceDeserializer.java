package hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import hekireki.sanjijiksong.global.common.exception.KamisException;

import java.io.IOException;

public class PriceDeserializer extends StdDeserializer<KamisDailyResponse.Data> {

    public PriceDeserializer() {
        this(null);
    }

    public PriceDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public KamisDailyResponse.Data deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonToken currentToken = jp.getCurrentToken();
        // data 필드가 배열인 경우
        if(currentToken == JsonToken.START_ARRAY){
            ArrayNode arrayNode = jp.getCodec().readTree(jp);

            if(arrayNode.size() > 0){
                String errorCode = arrayNode.get(0).asText();

                switch (errorCode){
                    case "001": // no data
                        throw new KamisException.KamisApiNoDataException();
                    case "200": // Wrong parameter
                        throw new KamisException.KamisApiWrongParameterException();
                    case "900": // Unauthenticated request
                        throw new KamisException.KamisApiUnauthenticatedException();
                }
            }
        }
        // 객체로 온 경우 기본 동작
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        return mapper.readValue(jp,KamisDailyResponse.Data.class);
    }
}

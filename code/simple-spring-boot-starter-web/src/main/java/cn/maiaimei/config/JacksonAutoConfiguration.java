package cn.maiaimei.config;

import java.math.BigDecimal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

public class JacksonAutoConfiguration {

  @Bean
  @Primary
  public JsonMapper jsonMapper() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(BigDecimal.class, new BigDecimalSerializer());
    module.addDeserializer(BigDecimal.class, new BigDecimalDeserializer());

    return JsonMapper.builder()
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addModule(module)
        .build();
  }

  static class BigDecimalSerializer extends ValueSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
      gen.writeString(value.toPlainString());
    }
  }

  static class BigDecimalDeserializer extends ValueDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
      return p.getDecimalValue();
    }
  }
}

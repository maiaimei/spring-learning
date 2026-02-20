package cn.maiaimei.config;

import static cn.maiaimei.constants.DateTimeConstants.DATE_FORMAT;
import static cn.maiaimei.constants.DateTimeConstants.TIME_FORMAT;
import static cn.maiaimei.constants.DateTimeConstants.UTC_DATE_TIME_FORMAT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Auto-configuration for Jackson JSON serialization and deserialization.
 */
public class JacksonAutoConfiguration {

  /**
   * Creates and configures the JsonMapper bean.
   *
   * @return configured JsonMapper instance
   */
  @Bean
  @Primary
  public JsonMapper jsonMapper() {
    SimpleModule module = new SimpleModule();

    // BigDecimal
    module.addSerializer(BigDecimal.class, new BigDecimalSerializer());
    module.addDeserializer(BigDecimal.class, new BigDecimalDeserializer());

    // LocalDateTime
    module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
    module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());

    // LocalDate
    module.addSerializer(LocalDate.class, new LocalDateSerializer());
    module.addDeserializer(LocalDate.class, new LocalDateDeserializer());

    // LocalTime
    module.addSerializer(LocalTime.class, new LocalTimeSerializer());
    module.addDeserializer(LocalTime.class, new LocalTimeDeserializer());

    return JsonMapper.builder()
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addModule(module)
        .build();
  }

  /**
   * Serializes BigDecimal as string to avoid precision loss in JSON.
   */
  static class BigDecimalSerializer extends ValueSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
      gen.writeString(value.toPlainString());
    }
  }

  /**
   * Deserializes string to BigDecimal.
   */
  static class BigDecimalDeserializer extends ValueDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
      return p.getDecimalValue();
    }
  }

  /**
   * Serializes LocalDateTime to string in yyyy-MM-dd'T'HH:mm:ss.SSS'Z' format.
   */
  static class LocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
      gen.writeString(value.format(DateTimeFormatter.ofPattern(UTC_DATE_TIME_FORMAT)));
    }
  }

  /**
   * Deserializes string to LocalDateTime from yyyy-MM-dd'T'HH:mm:ss.SSS'Z' format.
   */
  static class LocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
      return LocalDateTime.parse(p.getString(), DateTimeFormatter.ofPattern(UTC_DATE_TIME_FORMAT));
    }
  }

  /**
   * Serializes LocalDate to string in yyyy-MM-dd format.
   */
  static class LocalDateSerializer extends ValueSerializer<LocalDate> {

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
      gen.writeString(value.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
    }
  }

  /**
   * Deserializes string to LocalDate from yyyy-MM-dd format.
   */
  static class LocalDateDeserializer extends ValueDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
      return LocalDate.parse(p.getString(), DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
  }

  /**
   * Serializes LocalTime to string in HH:mm:ss format.
   */
  static class LocalTimeSerializer extends ValueSerializer<LocalTime> {

    @Override
    public void serialize(LocalTime value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
      gen.writeString(value.format(DateTimeFormatter.ofPattern(TIME_FORMAT)));
    }
  }

  /**
   * Deserializes string to LocalTime from HH:mm:ss format.
   */
  static class LocalTimeDeserializer extends ValueDeserializer<LocalTime> {

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
      return LocalTime.parse(p.getString(), DateTimeFormatter.ofPattern(TIME_FORMAT));
    }
  }
}

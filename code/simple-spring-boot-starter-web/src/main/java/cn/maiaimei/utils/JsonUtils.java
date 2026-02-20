package cn.maiaimei.utils;

import cn.maiaimei.config.JacksonAutoConfiguration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.util.CollectionUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

public final class JsonUtils {

  private static final JsonMapper jsonMapper = new JacksonAutoConfiguration().jsonMapper();

  /**
   * Private constructor to prevent instantiation.
   */
  private JsonUtils() {
  }

  public static String toJson(Object object) {
    if (Objects.isNull(object)) {
      return null;
    }
    return jsonMapper.writeValueAsString(object);
  }

  public static String toPrettyJson(Object object) {
    if (Objects.isNull(object)) {
      return null;
    }
    return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
  }

  public static <T> T toObject(String json, Class<T> clazz) {
    if (StringUtilsPlus.isEmpty(json)) {
      return null;
    }
    return jsonMapper.readValue(json, clazz);
  }

  public static <T> T toObject(String json, TypeReference<T> typeReference) {
    if (StringUtilsPlus.isEmpty(json)) {
      return null;
    }
    return jsonMapper.readValue(json, typeReference);
  }

  public static <T> List<T> toList(String json, Class<T> clazz) {
    if (StringUtilsPlus.isEmpty(json)) {
      return null;
    }
    return jsonMapper.readValue(json, jsonMapper.getTypeFactory().constructCollectionType(List.class, clazz));
  }

  public static Map<String, Object> toMap(String json) {
    if (StringUtilsPlus.isEmpty(json)) {
      return null;
    }
    return jsonMapper.readValue(json, new TypeReference<>() {
    });
  }

  public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
    if (CollectionUtils.isEmpty(map)) {
      return null;
    }
    return jsonMapper.convertValue(map, clazz);
  }

  public static <T> T deepCopy(T object, Class<T> clazz) {
    if (Objects.isNull(object)) {
      return null;
    }
    return jsonMapper.convertValue(object, clazz);
  }

  public static boolean isValidJson(String json) {
    if (StringUtilsPlus.isEmpty(json)) {
      return false;
    }
    try {
      jsonMapper.readTree(json);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}

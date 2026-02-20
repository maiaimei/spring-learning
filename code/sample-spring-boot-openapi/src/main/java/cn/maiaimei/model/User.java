package cn.maiaimei.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "User entity")
public class User {

  @Schema(description = "User ID", example = "1")
  private BigDecimal id;

  @Schema(description = "User name", example = "User name")
  private String name;
}

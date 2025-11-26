package com.example.model;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "User entity")
public class User {

  @Schema(description = "User ID", example = "1")
  private BigDecimal id;

  @Schema(description = "User name", example = "User name")
  private String name;
}

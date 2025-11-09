package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {

  private Long id;
  private String title;
  private String author;
  private String isbn;
  private BigDecimal price;
  private LocalDate publishDate;
  private String category;
  private Integer stock;
}
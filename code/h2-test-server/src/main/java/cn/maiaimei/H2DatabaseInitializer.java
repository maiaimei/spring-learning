package cn.maiaimei;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@DependsOn("jdbcTemplate")
public class H2DatabaseInitializer {

  private static final Logger log = LoggerFactory.getLogger(H2DatabaseInitializer.class);

  @Autowired
  private DataSource dataSource;

  @PostConstruct
  public void initDatabase() {
    log.info("H2数据库初始化开始");

    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.addScript(new ClassPathResource("scripts/schema.sql"));
    populator.addScript(new ClassPathResource("scripts/data.sql"));
    populator.execute(dataSource);

    log.info("H2数据库初始化完成");
  }
}
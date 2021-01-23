package com.mornati.sample.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "")
@Getter
@Setter
public class FelixConfiguration {

  private Map<String, String> pluginsService;
}

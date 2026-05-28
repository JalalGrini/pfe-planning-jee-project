package com.pfe.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.pfe")
@Import({DatabaseConfig.class, WebConfig.class})
public class AppConfig {
}

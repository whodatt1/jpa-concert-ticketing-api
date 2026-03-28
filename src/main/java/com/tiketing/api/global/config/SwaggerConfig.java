package com.tiketing.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
	
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Ticketing API Document")
						.description("티켓팅(콘서트) 서비스의 REST API 명세서입니다.")
						.version("v1.0.0"));
	}
}

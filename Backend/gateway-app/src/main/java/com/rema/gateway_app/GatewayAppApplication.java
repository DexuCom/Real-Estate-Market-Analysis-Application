package com.rema.gateway_app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;


@SpringBootApplication
@Configuration
public class GatewayAppApplication {
	@Value("${urls.web-api}")
	private String webApiURL;
	@Value("${urls.scoring-model}")
	private String scoringModelURL;

	public static void main(String[] args) {
		SpringApplication.run(GatewayAppApplication.class, args);
	}

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder)
	{
		return builder.routes()
				.route("gateway-check-route", r -> r
						.path("/gateway-check")
						.filters(f -> f
								.setStatus(HttpStatus.OK))
						.uri("no://op")
				)
				.route("scoring-model-route", r -> r
						.path("/api/scoring-model/**")
						.filters(f -> f.rewritePath("/api/scoring-model/(?<segment>.*)", "/${segment}"))
						.uri(scoringModelURL)
				)


				.route("webapi-route", r -> r
						.path("/api/**")
						.uri(webApiURL)
				)

				.build();
	}

}

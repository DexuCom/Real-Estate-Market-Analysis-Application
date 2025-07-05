package com.rema.gateway_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;


@SpringBootApplication
public class GatewayAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayAppApplication.class, args);
	}

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder)
	{
		return builder.routes()
				.route("gateway-check", r -> r
						.path("/gateway-check")
						.filters(f -> f
								.setStatus(HttpStatus.OK))
						.uri("no://op")
				)

				.build();
	}

}

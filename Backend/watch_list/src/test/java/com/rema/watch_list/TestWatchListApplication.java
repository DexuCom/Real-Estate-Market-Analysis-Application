package com.rema.watch_list;

import org.springframework.boot.SpringApplication;

public class TestWatchListApplication {

	public static void main(String[] args) {
		SpringApplication.from(WatchListApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

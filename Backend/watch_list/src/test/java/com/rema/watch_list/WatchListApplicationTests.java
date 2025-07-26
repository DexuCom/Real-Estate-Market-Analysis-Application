package com.rema.watch_list;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WatchListApplicationTests {

	@Test
	void contextLoads() {
	}

}

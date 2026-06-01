package com.nexotrade.os;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NexoTradeOsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexoTradeOsApplication.class, args);
	}

}

package com.example.alipay.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.example.alipay")
public class AlipayApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlipayApplication.class, args);
	}

}

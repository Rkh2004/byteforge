package com.byteforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.byteforge.auth.repository")
public class ByteforgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ByteforgeApplication.class, args);
	}

}

package com.example.springbootlogingmail;

import com.example.springbootlogingmail.config.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({SecurityConfig.class})
@SpringBootApplication
public class SpringBootLoginGmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootLoginGmailApplication.class, args);
	}

}

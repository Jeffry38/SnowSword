package com.leixiao.snowsword;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class SnowswordApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnowswordApplication.class, args);
	}

}

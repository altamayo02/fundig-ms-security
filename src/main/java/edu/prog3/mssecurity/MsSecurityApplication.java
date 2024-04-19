package edu.prog3.mssecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsSecurityApplication {
	// TODO - Ask whether response statuses should change based on whether the operation worked
	public static void main(String[] args) {
		SpringApplication.run(MsSecurityApplication.class, args);
	}
}

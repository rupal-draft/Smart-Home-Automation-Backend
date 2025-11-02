package com.shas.smart_home_automation_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SmartHomeAutomationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartHomeAutomationSystemApplication.class, args);
	}

}

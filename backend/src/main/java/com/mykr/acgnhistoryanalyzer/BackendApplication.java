package com.mykr.acgnhistoryanalyzer;

import com.mykr.acgnhistoryanalyzer.config.StartupDiagnosticsInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(BackendApplication.class);
		application.addInitializers(new StartupDiagnosticsInitializer());
		application.run(args);
	}

}

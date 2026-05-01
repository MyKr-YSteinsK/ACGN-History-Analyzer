package com.mykr.acgnhistoryanalyzer;

import com.mykr.acgnhistoryanalyzer.config.StartupDiagnosticsInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(BackendApplication.class);
		application.addInitializers(new StartupDiagnosticsInitializer());
		application.run(args);
	}

}

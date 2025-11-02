package es.blanca.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"es.blanca.api",
		"es.blanca.application",
		"es.blanca.jpa"
})
@EnableJpaRepositories(basePackages = "es.blanca.jpa.repository")
@EntityScan(basePackages = "es.blanca.jpa.entity")
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}

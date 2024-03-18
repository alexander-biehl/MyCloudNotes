package com.alexbiehl.mycloudnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class MycloudnotesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MycloudnotesApplication.class, args);
	}

	// @Bean
	// public WebMvcConfigurer corsConfigurer() {
	// return new WebMvcConfigurer() {
	// @Override
	// public void addCorsMappings(@NonNull final CorsRegistry registry) {
	// //
	// registry.addMapping("/greeting-javaconfig").allowedOrigins("http://localhost:9000");
	// // registry.addMapping("/notes").allowedOrigins("http://localhost:5173");
	// registry.addMapping("/api/notes")
	// .allowedOriginPatterns("http://localhost:*")
	// .allowedOrigins("http://localhost:5173")
	// .allowedMethods("GET", "POST", "OPTIONS", "PUT")
	// .allowedHeaders("Content-Type", "Accept", "Origin",
	// "Access-Control-Request-Method",
	// "Access-Control-Request-Headers")
	// .exposedHeaders("Access-Control-Allow-Origin",
	// "Access-Control-Allow-Credentials");
	// registry.addMapping("/**")
	// .allowedOriginPatterns("http://localhost:*")
	// .allowedOrigins("http://localhost:5173")
	// .allowedMethods("GET", "POST", "OPTIONS", "PUT")
	// .allowedHeaders("Content-Type", "Accept", "Origin",
	// "Access-Control-Request-Method",
	// "Access-Control-Request-Headers")
	// .exposedHeaders("Access-Control-Allow-Origin",
	// "Access-Control-Allow-Credentials");
	// }
	// };
	// }

}

package edu.prog3.mssecurity.Configurations;

import edu.prog3.mssecurity.Interceptors.SecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private SecurityInterceptor securityInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(securityInterceptor)
			.addPathPatterns("/api/**")
			.excludePathPatterns("/api/public/**", "/api/users/create");
		// Use MongoDB Shell if having problems with permissions
    }
}
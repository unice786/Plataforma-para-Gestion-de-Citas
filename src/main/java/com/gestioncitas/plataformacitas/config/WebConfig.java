package com.gestioncitas.plataformacitas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutorizacionAdministrador autorizacionAdministrador;

    public WebConfig(AutorizacionAdministrador autorizacionAdministrador) {
        this.autorizacionAdministrador = autorizacionAdministrador;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autorizacionAdministrador)
                .addPathPatterns("/admin/**");
    }
}

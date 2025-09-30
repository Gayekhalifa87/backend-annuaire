package com.annuaire.khalifa.annuaire.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Configuration
@EnableFeignClients(basePackages = "com.annuaire.khalifa.annuaire.external")
public class FeignConfig {
}
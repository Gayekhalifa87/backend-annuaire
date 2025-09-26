package com.annuaire.khalifa.annuaire.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.annuaire.khalifa.annuaire.external")
public class FeignConfig {
}
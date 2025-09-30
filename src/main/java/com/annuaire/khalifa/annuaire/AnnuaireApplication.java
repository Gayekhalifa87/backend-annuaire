package com.annuaire.khalifa.annuaire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients
public class AnnuaireApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnnuaireApplication.class, args);
	}
}

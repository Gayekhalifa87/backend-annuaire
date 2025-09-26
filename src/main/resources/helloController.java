package com.annuaire.khalifa.annuaire;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class helloController {

    @GetMapping("/")
    public String hello(){
        return "mon controlleur mache";
    }
}

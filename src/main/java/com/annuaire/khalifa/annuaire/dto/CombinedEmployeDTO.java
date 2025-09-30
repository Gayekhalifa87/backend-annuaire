package com.annuaire.khalifa.annuaire.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CombinedEmployeDTO {
    // Infos internes
    private int id;
    private Integer ip;
    private String telephone;
    private String role;

    // Infos externes
    private String nom;
    private String matricule;
    private String prenom;
    private String email;
    private String direction;
    private String service;
    private String poste;
}

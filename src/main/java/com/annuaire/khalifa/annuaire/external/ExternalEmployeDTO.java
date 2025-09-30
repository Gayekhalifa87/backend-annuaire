package com.annuaire.khalifa.annuaire.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalEmployeDTO {
    private int id;
    private String nom;
    private String prenom;
    private String matricule;
    private String email;
    private String direction;
    private String service;
    private String poste;
}
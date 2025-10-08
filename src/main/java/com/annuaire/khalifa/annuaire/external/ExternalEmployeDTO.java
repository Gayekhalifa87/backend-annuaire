
// ===== ExternalEmployeDTO.java =====
package com.annuaire.khalifa.annuaire.external;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalEmployeDTO {
    private int id;
    private String nom;
    private String prenom;
    private String matricule;
    private String email;
    private String direction;
    private String service;
    private String poste;
    private ChefInfo chef; // ✅ Hiérarchie

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChefInfo {
        private Long id;
        private Integer matricule;
        private String fullName;
        private String email;
        private String fonction;
        private String direction;
        private ChefInfo chef; // ✅ Récursif pour toute la hiérarchie
    }
}
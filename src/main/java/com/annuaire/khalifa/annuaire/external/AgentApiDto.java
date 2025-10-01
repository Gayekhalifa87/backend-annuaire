package com.annuaire.khalifa.annuaire.external;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AgentApiDto {
    private Long id;
    private String fullName;
    private Integer matricule;
    private String email;
    private String telephone;
    private DirectionDto direction;
    private FonctionDto fonction;
    private RattachementDto rattachement;

    // Classe interne pour Direction
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DirectionDto {
        private Long id;
        private Boolean active;
        private String name;
        private String code;
    }

    // Classe interne pour Fonction
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FonctionDto {
        private Long id;
        private Boolean active;
        private String name;
        private String code;
    }

    // Classe interne pour Rattachement (SERVICE)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RattachementDto {
        private Long id;
        private Boolean active;
        private String code;
        private String name;
    }
}
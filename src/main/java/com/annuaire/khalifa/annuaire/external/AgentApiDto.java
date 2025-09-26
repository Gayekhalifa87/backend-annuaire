// Créer ce fichier : src/main/java/com/annuaire/khalifa/annuaire/external/AgentApiDto.java

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
    private String email;
    private String telephone;
    private DirectionDto direction;
    private FonctionDto fonction;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DirectionDto {
        private String nom;
        private String code;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FonctionDto {
        private String nom;
        private String code;
    }
}
// REMPLACER complètement le contenu de votre fichier ExternalApiMockService.java existant

package com.annuaire.khalifa.annuaire.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

@Service
@RequiredArgsConstructor
public class ExternalApiMockService {

    private final AgentApiClient agentApiClient;

    public ExternalEmployeDTO getExternalEmploye(int externalId) {
        try {
            // Appel à la vraie API via Feign
            AgentApiDto agent = agentApiClient.getAgentById((long) externalId);
            return mapToExternalEmployeDTO(agent);
        } catch (Exception e) {
            // En cas d'erreur, on peut retourner un mock ou null
            System.err.println("Erreur API externe pour l'agent " + externalId + ": " + e.getMessage());

            // Option 1 : Retourner null
            return null;


        }
    }

    // Méthode utilitaire pour convertir AgentApiDto vers ExternalEmployeDTO
    private ExternalEmployeDTO mapToExternalEmployeDTO(AgentApiDto agent) {
        if (agent == null) return null;

        ExternalEmployeDTO dto = new ExternalEmployeDTO();
        dto.setId(agent.getId().intValue());


        // Séparer le fullName en nom et prénom
        if (agent.getFullName() != null && !agent.getFullName().trim().isEmpty()) {
            String[] nameParts = agent.getFullName().trim().split("\\s+", 2);
            if (nameParts.length >= 2) {
                dto.setPrenom(nameParts[0]);
                dto.setNom(nameParts[1]);
            } else if (nameParts.length == 1) {
                dto.setPrenom(nameParts[0]);
                dto.setNom("");
            }
        }

        dto.setEmail(agent.getEmail());

        // Direction
        if (agent.getDirection() != null && agent.getDirection().getNom() != null) {
            dto.setDirection(agent.getDirection().getNom());
        }

        // Service et Poste depuis la fonction
        if (agent.getFonction() != null && agent.getFonction().getNom() != null) {
            dto.setService(agent.getFonction().getNom());
            dto.setPoste(agent.getFonction().getNom());
        }

        return dto;
    }
}
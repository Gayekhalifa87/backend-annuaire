// ===== ExternalApiMockService.java =====
package com.annuaire.khalifa.annuaire.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalApiMockService {

    private final AgentApiClient agentApiClient;

    public ExternalEmployeDTO getExternalEmploye(int externalId) {
        try {
            AgentApiDto agent = agentApiClient.getAgentById((long) externalId);
            return mapToExternalEmployeDTO(agent);
        } catch (Exception e) {
            System.err.println("❌ Erreur API externe pour l'agent " + externalId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private ExternalEmployeDTO mapToExternalEmployeDTO(AgentApiDto agent) {
        if (agent == null) {
            System.err.println("⚠️ Agent null reçu");
            return null;
        }

        ExternalEmployeDTO dto = new ExternalEmployeDTO();
        dto.setId(agent.getId().intValue());

        // 1. Matricule
        if (agent.getMatricule() != null) {
            dto.setMatricule(String.valueOf(agent.getMatricule()));
        }

        // 2. Email
        dto.setEmail(agent.getEmail());

        // 3. Séparer le fullName en nom et prénom
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

        // 4. Direction
        if (agent.getDirection() != null && agent.getDirection().getName() != null) {
            dto.setDirection(agent.getDirection().getName());
        }

        // 5. Service
        if (agent.getRattachement() != null && agent.getRattachement().getName() != null) {
            dto.setService(agent.getRattachement().getName());
        }

        // 6. Poste
        if (agent.getFonction() != null && agent.getFonction().getName() != null) {
            dto.setPoste(agent.getFonction().getName());
        }

        // 7. ✅ Hiérarchie complète (récursive)
        if (agent.getChef() != null) {
            dto.setChef(mapChefInfo(agent.getChef()));
        }

        return dto;
    }

    // ✅ Méthode récursive pour mapper toute la hiérarchie
    private ExternalEmployeDTO.ChefInfo mapChefInfo(AgentApiDto.ChefDto chef) {
        if (chef == null) return null;

        ExternalEmployeDTO.ChefInfo chefInfo = new ExternalEmployeDTO.ChefInfo();
        chefInfo.setId(chef.getId());
        chefInfo.setMatricule(chef.getMatricule());
        chefInfo.setFullName(chef.getFullName());
        chefInfo.setEmail(chef.getEmail());

        // Fonction du chef
        if (chef.getFonction() != null && chef.getFonction().getName() != null) {
            chefInfo.setFonction(chef.getFonction().getName());
        }

        // Direction du chef
        if (chef.getDirection() != null && chef.getDirection().getName() != null) {
            chefInfo.setDirection(chef.getDirection().getName());
        }

        // ✅ Récursion : chef du chef
        if (chef.getChef() != null) {
            chefInfo.setChef(mapChefInfo(chef.getChef()));
            System.out.println("✅ Hiérarchie : " + chef.getFullName() + " -> " + chef.getChef().getFullName());
        }

        return chefInfo;
    }
}
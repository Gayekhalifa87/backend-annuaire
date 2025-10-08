package com.annuaire.khalifa.annuaire.controllers;

import com.annuaire.khalifa.annuaire.dto.CombinedEmployeDTO;
import com.annuaire.khalifa.annuaire.dto.LoginDTO;
import com.annuaire.khalifa.annuaire.external.ExternalApiMockService;
import com.annuaire.khalifa.annuaire.external.ExternalEmployeDTO;
import com.annuaire.khalifa.annuaire.models.Employe;
import com.annuaire.khalifa.annuaire.services.EmailService;
import com.annuaire.khalifa.annuaire.services.EmployeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employes")
public class EmployeController {
    private final EmployeService employeService;
    private final EmailService emailService;
    private final ExternalApiMockService externalApiMockService;

    public EmployeController(EmployeService employeService, EmailService emailService, ExternalApiMockService externalApiMockService) {
        this.employeService = employeService;
        this.emailService = emailService;
        this.externalApiMockService = externalApiMockService;
    }

    //POUR COMBINER LES DEUX BASES DE DONNEES
    @GetMapping("/combined/{id}")
    public ResponseEntity<CombinedEmployeDTO> getCombinedEmploye(@PathVariable int id) {
        CombinedEmployeDTO dto = employeService.getCombinedEmploye(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //TOUS LES EMPLOYES(MOCK +BASE INTERNE)
    @GetMapping("/combined")
    public List<CombinedEmployeDTO> getAllCombinedEmployes() {
        return employeService.getAllCombinedEmployes();
    }

    @GetMapping
    public List<Employe> getAllEmployes() {
        return employeService.getAllEmployes();
    }

    @GetMapping("/count")
    public long getTotalEmployes() {
        return employeService.getTotalEmployes();
    }

    @GetMapping("/search")
    public ResponseEntity<CombinedEmployeDTO> searchCombinedByIp(@RequestParam int ip) {
        // Cherche dans la base interne
        Optional<Employe> internalOpt = employeService.findByIp(ip);

        if (internalOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // On récupère l'employé interne
        Employe internal = internalOpt.get();

        // Récupère les infos de la base externe (maintenant via la vraie API)
        ExternalEmployeDTO external = externalApiMockService.getExternalEmploye(internal.getEmployeId());

        if (external == null) {
            return ResponseEntity.notFound().build();
        }

        // Combine les deux en DTO
        CombinedEmployeDTO combined = new CombinedEmployeDTO();
        combined.setId(internal.getId());
        combined.setNom(external.getNom());
        combined.setPrenom(external.getPrenom());
        combined.setMatricule(external.getMatricule());
        combined.setEmail(external.getEmail());
        combined.setIp(internal.getIp());
        combined.setTelephone(internal.getTelephone());
        combined.setRole(internal.getRole());
        combined.setPoste(external.getPoste());
        combined.setDirection(external.getDirection());
        combined.setService(external.getService());

        return ResponseEntity.ok(combined);
    }

//LA SUPPRESSION
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteEmploye(@PathVariable int id) {
    boolean deleted = employeService.deleteEmploye(id);
    if (deleted) {
        return ResponseEntity.noContent().build(); // 204
    } else {
        return ResponseEntity.notFound().build(); // 404 si non trouvé
    }
}


    @GetMapping("/{id}")
    public Optional<Employe> findById(@PathVariable int id) {
        return employeService.findById(id);
    }

//    @PostMapping
//    //Creation d un nouvel employe
//    public Employe createEmploye(@RequestBody Employe employe) {
//        return employeService.createEmploye(employe);
//    }
@PostMapping
public ResponseEntity<?> createEmploye(@RequestBody Employe employe) {
    try {
        Employe saved = employeService.createEmploye(employe);
        return ResponseEntity.ok(saved);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}


    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmploye(
            @PathVariable int id,
            @RequestBody Employe updatedEmploye) {
        try {
            Employe existing = employeService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

            // Vérifier si la nouvelle IP est déjà utilisée par UN AUTRE employé
            if (updatedEmploye.getIp() != null) {
                Optional<Employe> ipOwner = employeService.findByIp(updatedEmploye.getIp());
                if (ipOwner.isPresent() && ipOwner.get().getId() != id) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Cette IP est déjà attribuée à un autre employé."));
                }
                existing.setIp(updatedEmploye.getIp());
            }

            // Vérifier si le nouveau téléphone est déjà utilisé par UN AUTRE employé
            if (updatedEmploye.getTelephone() != null) {
                Optional<Employe> phoneOwner = employeService.findByTelephone(updatedEmploye.getTelephone());
                if (phoneOwner.isPresent() && phoneOwner.get().getId() != id) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Ce téléphone est déjà attribué à un autre employé."));
                }
                existing.setTelephone(updatedEmploye.getTelephone());
            }

            // Mise à jour du mot de passe si fourni
            if (updatedEmploye.getPassword() != null && !updatedEmploye.getPassword().isEmpty()) {
                existing.setPassword(employeService.encodePassword(updatedEmploye.getPassword()));
            }

            Employe saved = employeService.save(existing);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Erreur serveur"));
        }
    }


    @PatchMapping("/{id}")
    public ResponseEntity<Employe> changeRole(@PathVariable int id) {
        return employeService.findById(id)
                .map(employeAvant -> {
                    String ancienRole = employeAvant.getRole();
                    // Change le rôle
                    Employe updated = employeService.changeRole(id);

                    // Email uniquement si USER → ADMIN
                    if ("USER".equalsIgnoreCase(ancienRole) && "ADMIN".equalsIgnoreCase(updated.getRole())) {
                        String to = employeService.getEmailFromExternal(updated.getEmployeId());

                        String subject = "Changement de rôle";
                        String body = "Bonjour " + updated.getIp() + ", toutes nos félicitations ! Vous êtes désormais administrateur.";

                        emailService.sendSimpleEmail(to, subject, body);
                    }
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response, @RequestHeader("Authorization") String authHeader) throws IOException {
        // Supprime le token côté serveur (facultatif si JWT)

        // Redirige vers Keycloak pour terminer la session
        String logoutUrl = "https://refonte.seneau.sn/realms/auth2-dev/protocol/openid-connect/logout?redirect_uri=http://localhost:4200";
        response.sendRedirect(logoutUrl);
    }

}
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
    //TESTONS LE MOCK
    @GetMapping("/test-mock/{externalId}")
    public ExternalEmployeDTO testExternalMock(@PathVariable Integer externalId) {
        return externalApiMockService.getExternalEmploye(externalId);
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

    // Récupère les infos de la base externe via le mock
    ExternalEmployeDTO external = externalApiMockService.getExternalEmploye(internal.getEmployeId());

    // Combine les deux en DTO
    CombinedEmployeDTO combined = new CombinedEmployeDTO();
    combined.setId(internal.getId());
    combined.setNom(external.getNom());
    combined.setPrenom(external.getPrenom());
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
    //@PathVariable int id → récupère la valeur de {id} de l’URL et la passe à ta méthode
    public Optional<Employe> findById(@RequestBody @PathVariable int id) {

        return employeService.findById(id);
    }

    @PostMapping
    //Creation d un nouvel employe
    public Employe createEmploye(@RequestBody Employe employe) {
        return employeService.createEmploye(employe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employe> updateEmploye(
            @PathVariable int id,
            @RequestBody Employe updatedEmploye) {

        try {
            Employe existing = employeService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

            if (updatedEmploye.getIp() != null) {
                existing.setIp(updatedEmploye.getIp());
            }

            if (updatedEmploye.getTelephone() != null) {
                existing.setTelephone(updatedEmploye.getTelephone());
            }

            if (updatedEmploye.getPassword() != null) {
                existing.setPassword(employeService.encodePassword(updatedEmploye.getPassword()));
            }

            Employe saved = employeService.save(existing);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
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
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        String tokenUrl = "https://refonte.seneau.sn/realms/auth2-dev/protocol/openid-connect/token";

        // ⚡ Corps de la requête en x-www-form-urlencoded
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", "seneau"); // ton client_id
        formData.add("client_secret", "eLDu7SfmCjSGlI7YOFXp7xZtgJi73mhF"); // ton secret
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            return ResponseEntity.ok(response.getBody()); // renvoie directement le token JSON
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Login failed", "details", e.getMessage()));
        }
    }


    @PostMapping("/logout")
    public void logout(HttpServletResponse response, @RequestHeader("Authorization") String authHeader) throws IOException {
        // Supprime le token côté serveur (facultatif si JWT)

        // Redirige vers Keycloak pour terminer la session
        String logoutUrl = "https://refonte.seneau.sn/realms/auth2-dev/protocol/openid-connect/logout?redirect_uri=http://localhost:4200";
        response.sendRedirect(logoutUrl);
    }

}
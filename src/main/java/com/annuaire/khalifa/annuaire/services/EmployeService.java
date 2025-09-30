package com.annuaire.khalifa.annuaire.services;

import com.annuaire.khalifa.annuaire.dto.CombinedEmployeDTO;
import com.annuaire.khalifa.annuaire.external.ExternalApiMockService;
import com.annuaire.khalifa.annuaire.models.Employe;
import com.annuaire.khalifa.annuaire.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeService {
    private final EmployeRepository employeRepository ;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ExternalApiMockService externalApiMockService;

    public EmployeService(EmployeRepository employeRepository, PasswordEncoder passwordEncoder) {
        this.employeRepository = employeRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public List<Employe> getAllEmployes() {

        return employeRepository.findAll();
    }
    public Optional<Employe> findById(int id) {

        return employeRepository.findById(id);
    }
    public Optional<Employe> findByIp(int ip) {

        return employeRepository.findByIp(ip);
    }
    public Optional<Employe> findByEmployeId(int employeId) {

        return employeRepository.findByEmployeId(employeId);
    }
    public Optional<Employe> findByTelephone(String telephone) {

        return employeRepository.findByTelephone(telephone);
    }

    //Creation d un nouvel employe
//    public Employe createEmploye(Employe employe) {
//        employe.setPassword(passwordEncoder.encode(employe.getPassword()));
//        return employeRepository.save(employe);
//    }

    public Employe createEmploye(Employe employe) {
        // Vérifier si cet agent externe est déjà assigné
        if (employe.getEmployeId() != null && employeRepository.existsByEmployeId(employe.getEmployeId())) {
            throw new IllegalArgumentException("Cet agent a déjà été assigné dans l'annuaire local.");
        }

        // Vérifier si l'IP est déjà utilisée par UN AUTRE employé
        if (employe.getIp() != null) {
            employeRepository.findByIp(employe.getIp()).ifPresent(existing -> {
                if (!existing.getEmployeId().equals(employe.getEmployeId())) {
                    throw new IllegalArgumentException("Cette IP est déjà attribuée à un autre employé.");
                }
            });
        }

        // Vérifier si le téléphone est déjà utilisé par UN AUTRE employé
        if (employe.getTelephone() != null && !employe.getTelephone().isEmpty()) {
            employeRepository.findByTelephone(employe.getTelephone()).ifPresent(existing -> {
                if (!existing.getEmployeId().equals(employe.getEmployeId())) {
                    throw new IllegalArgumentException("Ce numéro de téléphone est déjà attribué à un autre employé.");
                }
            });
        }

        employe.setPassword(passwordEncoder.encode(employe.getPassword()));
        return employeRepository.save(employe);
    }

    // Suppression d’un employé
    public boolean deleteEmploye(int id) {
        return employeRepository.findById(id)
                .map(e -> {
                    employeRepository.delete(e);
                    return true;
                })
                .orElse(false);
    }
    public boolean updateEmploye(int id, int newIp, String newPassword, String newTelephone) {
        return employeRepository.findById(id)
                .map(e -> {
                    e.setIp(newIp);
                    e.setTelephone(newTelephone);
                    e.setPassword(passwordEncoder.encode(newPassword));
                    employeRepository.save(e);
                    return true;
                })
                .orElse(false);
    }

    public Employe changeRole(int id) {
        return employeRepository.findById(id)
                .map(e -> {
                    e.setRole(e.getRole().equals("USER") ? "ADMIN" : "USER");
                    return employeRepository.save(e);
                })
                .orElse(null);
    }


    // Méthode pour récupérer le nombre total d'employés
    public long getTotalEmployes() {

        return employeRepository.count();
    }

    // Récupérer l'email depuis l'API externe simulée
    public String getEmailFromExternal(Integer externalId) {
        if (externalId == null) return null;
        return externalApiMockService.getExternalEmploye(externalId).getEmail();
    }

    //OBTENIR LES EMPLOYES COMBINES PAR ID
    public CombinedEmployeDTO getCombinedEmploye(int id) {
        return employeRepository.findById(id)
                .map(employe -> {
                    CombinedEmployeDTO dto = new CombinedEmployeDTO();

                    // Infos internes
                    dto.setId(employe.getId());
                    dto.setIp(employe.getIp());
                    dto.setTelephone(employe.getTelephone());
                    dto.setRole(employe.getRole());

                    // Infos externes
                    if (employe.getEmployeId() != null) {
                        var external = externalApiMockService.getExternalEmploye(employe.getEmployeId());
                        dto.setNom(external.getNom());
                        dto.setPrenom(external.getPrenom());
                        dto.setEmail(external.getEmail());
                        dto.setDirection(external.getDirection());
                        dto.setService(external.getService());
                        dto.setPoste(external.getPoste());
                    }

                    return dto;
                })
                .orElse(null);
    }

    //OBTENIR TOUS LES EMPLOYES EN COMBINANT LES DEUX BASES DE DONNEES

    public List<CombinedEmployeDTO> getAllCombinedEmployes() {
        return employeRepository.findAll()
                .stream()
                .map(employe -> {
                    CombinedEmployeDTO dto = new CombinedEmployeDTO();

                    // Infos internes
                    dto.setId(employe.getId());
                    dto.setIp(employe.getIp());
                    dto.setTelephone(employe.getTelephone());
                    dto.setRole(employe.getRole());

                    // Infos externes
                    if (employe.getEmployeId() != null) {
                        var external = externalApiMockService.getExternalEmploye(employe.getEmployeId());
                        dto.setNom(external.getNom());
                        dto.setPrenom(external.getPrenom());
                        dto.setEmail(external.getEmail());
                        dto.setDirection(external.getDirection());
                        dto.setService(external.getService());
                        dto.setPoste(external.getPoste());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }



    // Recherche via le mock
    public Optional<Employe> findByEmailMock(String email) {
        return employeRepository.findAll()
                .stream()
                .filter(e -> {
                    if (e.getEmployeId() == null) return false;
                    String externalEmail = externalApiMockService.getExternalEmploye(e.getEmployeId()).getEmail();
                    return email.equalsIgnoreCase(externalEmail);
                })
                .findFirst();
    }

    // Vérifie le mot de passe
    public boolean checkPassword(Employe employe, String rawPassword) {
        return passwordEncoder.matches(rawPassword, employe.getPassword());
    }


    //POUR LA MODIF
    public Employe save(Employe employe) {
        return employeRepository.save(employe);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }



}
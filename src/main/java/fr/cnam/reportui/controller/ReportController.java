package fr.cnam.reportui.controller;

import fr.cnam.reportui.dto.CreateReportRequest;
import fr.cnam.reportui.dto.ProjectReportDto;
import fr.cnam.reportui.dto.ReportSummaryDto;
import fr.cnam.reportui.service.ReportStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des rapports de migration.
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Gestion des rapports de migration")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportStorageService storageService;

    public ReportController(ReportStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Soumettre un nouveau rapport", description = "Crée un nouveau rapport de migration")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Rapport créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ProjectReportDto createReport(
            @RequestBody CreateReportRequest request) {
        try {
            return storageService.save(request);
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur lors de la sauvegarde du rapport: " + e.getMessage()
            );
        }
    }

    @GetMapping
    @Operation(summary = "Lister tous les rapports", description = "Retourne la liste des résumés de tous les rapports")
    @ApiResponse(responseCode = "200", description = "Liste des rapports")
    public List<ReportSummaryDto> listReports() {
        return storageService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un rapport", description = "Retourne le rapport complet avec toutes les bibliothèques")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rapport trouvé"),
        @ApiResponse(responseCode = "404", description = "Rapport non trouvé")
    })
    public ProjectReportDto getReport(
            @Parameter(description = "Identifiant du rapport") @PathVariable String id) {
        return storageService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Rapport non trouvé: " + id
            ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un rapport", description = "Supprime un rapport de migration")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Rapport supprimé"),
        @ApiResponse(responseCode = "404", description = "Rapport non trouvé")
    })
    public ResponseEntity<Void> deleteReport(
            @Parameter(description = "Identifiant du rapport") @PathVariable String id) {
        if (!storageService.exists(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport non trouvé: " + id);
        }
        storageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

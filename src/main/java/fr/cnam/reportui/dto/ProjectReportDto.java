package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Rapport complet de migration d'un projet.
 */
@Schema(description = "Rapport de migration complet")
public record ProjectReportDto(
    @Schema(description = "Identifiant unique du rapport", example = "550e8400-e29b-41d4-a716-446655440000")
    String id,

    @Schema(description = "Nom du projet migré", example = "GMIC_J")
    String projectName,

    @Schema(description = "Date et heure de la migration")
    LocalDateTime migrationDate,

    @Schema(description = "Statistiques agrégées")
    StatisticsDto statistics,

    @Schema(description = "Liste de toutes les bibliothèques")
    List<LibraryInfoDto> libraries,

    @Schema(description = "Liste des JARs détectés dans le projet source")
    List<DetectedJarDto> detectedJars,

    @Schema(description = "Liste des packages manquants (si compilation échouée)")
    List<MissingPackageDto> missingPackages,

    @Schema(description = "Indique si le projet compile avec succès")
    Boolean compilationSuccess
) {}

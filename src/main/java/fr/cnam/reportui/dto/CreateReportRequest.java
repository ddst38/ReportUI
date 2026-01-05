package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Requête de création d'un nouveau rapport de migration.
 */
@Schema(description = "Requête de création de rapport")
public record CreateReportRequest(
    @Schema(description = "Nom du projet migré", example = "GMIC_J", required = true)
    String projectName,

    @Schema(description = "Date et heure de la migration")
    LocalDateTime migrationDate,

    @Schema(description = "Statistiques agrégées", required = true)
    StatisticsDto statistics,

    @Schema(description = "Liste de toutes les bibliothèques", required = true)
    List<LibraryInfoDto> libraries
) {}

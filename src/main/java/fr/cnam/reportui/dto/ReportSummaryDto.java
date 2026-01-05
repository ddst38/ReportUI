package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Résumé d'un rapport pour la liste.
 */
@Schema(description = "Résumé d'un rapport de migration")
public record ReportSummaryDto(
    @Schema(description = "Identifiant unique du rapport", example = "550e8400-e29b-41d4-a716-446655440000")
    String id,

    @Schema(description = "Nom du projet migré", example = "GMIC_J")
    String projectName,

    @Schema(description = "Date et heure de la migration")
    LocalDateTime migrationDate,

    @Schema(description = "Nombre total de JARs", example = "85")
    int totalJars,

    @Schema(description = "Nombre de dépendances résolues", example = "72")
    int resolved,

    @Schema(description = "Nombre de dépendances non résolues", example = "13")
    int unresolved,

    @Schema(description = "Taux de succès en pourcentage", example = "84.7")
    double successRate
) {
    /**
     * Crée un résumé à partir d'un rapport complet.
     */
    public static ReportSummaryDto fromReport(ProjectReportDto report) {
        return new ReportSummaryDto(
            report.id(),
            report.projectName(),
            report.migrationDate(),
            report.statistics().totalJars(),
            report.statistics().resolved(),
            report.statistics().unresolved(),
            report.statistics().successRate()
        );
    }
}

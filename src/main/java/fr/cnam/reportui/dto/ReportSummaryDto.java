package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

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

    @Schema(description = "Nombre de dépendances non résolues internes", example = "5")
    Integer unresolvedInternal,

    @Schema(description = "Nombre de dépendances non résolues externes", example = "8")
    Integer unresolvedExternal,

    @Schema(description = "Nombre de dépendances provided via auto-fix", example = "3")
    Integer providedAutoFix,

    @Schema(description = "Nombre de packages manquants distincts", example = "2")
    Integer missingCount,

    @Schema(description = "Taux de succès en pourcentage", example = "84.7")
    double successRate,

    @Schema(description = "Taux de couverture en pourcentage", example = "98.6")
    Double coverageRate,

    @Schema(description = "Indique si le projet compile avec succès")
    Boolean compilationSuccess,

    @Schema(description = "Résumé CVE (si analyse effectuée)")
    CveSummaryDto cveSummary,

    @Schema(description = "Résolus depuis repos standards")
    Integer resolvedStd,

    @Schema(description = "Résolus internes depuis migration-java-dette")
    Integer resolvedInt,

    @Schema(description = "Résolus externes depuis migration-java-dette")
    Integer resolvedExt,

    @Schema(description = "Indique si le repo migration-java-dette est utilisé")
    Boolean hasMigrationRepo,

    @Schema(description = "Indique si Artifactory a été utilisé pour la résolution")
    Boolean artifactoryEnabled,

    @Schema(description = "Indique si Nexus a été utilisé pour la résolution")
    Boolean nexusEnabled,

    @Schema(description = "Nombre de librairies déployées (mode REMOTE)")
    Integer deployedLibrariesCount,

    @Schema(description = "Répartition par localisation source")
    Map<String, Integer> byLocation
) {
    /**
     * Crée un résumé à partir d'un rapport complet.
     */
    public static ReportSummaryDto fromReport(ProjectReportDto report) {
        // Détecter si Artifactory ou Nexus ont été utilisés via byMethod
        var byMethod = report.statistics().byMethod();
        boolean artifactory = byMethod != null && (
            byMethod.containsKey("ARTIFACTORY") ||
            byMethod.containsKey("ARTIFACTORY_CHECKSUM")
        );
        boolean nexus = byMethod != null && (
            byMethod.containsKey("NEXUS") ||
            byMethod.containsKey("NEXUS_CHECKSUM")
        );

        // Nombre de librairies déployées
        Integer deployedCount = null;
        if (report.deploymentInfo() != null) {
            deployedCount = report.deploymentInfo().deployedCount();
        }

        return new ReportSummaryDto(
            report.id(),
            report.projectName(),
            report.migrationDate(),
            report.statistics().totalJars(),
            report.statistics().resolved(),
            report.statistics().unresolved(),
            report.statistics().unresolvedInternal(),
            report.statistics().unresolvedExternal(),
            report.statistics().providedAutoFix(),
            report.statistics().missingCount(),
            report.statistics().successRate(),
            report.statistics().coverageRate(),
            report.compilationSuccess(),
            report.cveSummary(),
            report.statistics().resolvedStd(),
            report.statistics().resolvedInt(),
            report.statistics().resolvedExt(),
            report.statistics().hasMigrationRepo(),
            artifactory,
            nexus,
            deployedCount,
            report.statistics().byLocation()
        );
    }

    /**
     * Retourne la sévérité CVE maximale ou null.
     */
    public String maxCveSeverity() {
        return cveSummary != null ? cveSummary.maxSeverity() : null;
    }

    /**
     * Retourne la couleur de la sévérité CVE maximale ou null.
     */
    public String maxCveSeverityColor() {
        return cveSummary != null ? cveSummary.maxSeverityColor() : null;
    }

    /**
     * Vérifie si le projet a des vulnérabilités CVE.
     */
    public boolean hasCveVulnerabilities() {
        return cveSummary != null && cveSummary.hasVulnerabilities();
    }
}

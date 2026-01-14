package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Résumé des vulnérabilités CVE d'un projet.
 */
@Schema(description = "Résumé des vulnérabilités CVE")
public record CveSummaryDto(
    @Schema(description = "Nombre total de vulnérabilités", example = "15")
    int totalVulnerabilities,

    @Schema(description = "Nombre de vulnérabilités critiques (CVSS >= 9)", example = "2")
    int criticalCount,

    @Schema(description = "Nombre de vulnérabilités hautes (CVSS >= 7)", example = "5")
    int highCount,

    @Schema(description = "Nombre de vulnérabilités moyennes (CVSS >= 4)", example = "6")
    int mediumCount,

    @Schema(description = "Nombre de vulnérabilités basses (CVSS < 4)", example = "2")
    int lowCount,

    @Schema(description = "Sévérité maximale", example = "CRITICAL")
    String maxSeverity,

    @Schema(description = "Couleur de la sévérité maximale", example = "#1a1a1a")
    String maxSeverityColor,

    @Schema(description = "Score de risque global", example = "47")
    int riskScore,

    @Schema(description = "Nombre de librairies affectées", example = "8")
    int affectedLibraries,

    @Schema(description = "Top 5 des librairies les plus vulnérables (GAV -> nombre CVE)")
    Map<String, Integer> topVulnerableLibraries
) {
    /**
     * Vérifie si le projet a des vulnérabilités critiques.
     */
    public boolean hasCriticalVulnerabilities() {
        return criticalCount > 0;
    }

    /**
     * Vérifie si le projet a des vulnérabilités.
     */
    public boolean hasVulnerabilities() {
        return totalVulnerabilities > 0;
    }
}

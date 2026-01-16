package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Resultat complet de l'analyse SonarQube.
 */
@Schema(description = "Analyse SonarQube du projet")
public record SonarAnalysisDto(
    @Schema(description = "Cle du projet SonarQube")
    String projectKey,

    @Schema(description = "Date de la derniere analyse")
    String analysisDate,

    @Schema(description = "Statut du Quality Gate (OK, WARN, ERROR)")
    String qualityGateStatus,

    @Schema(description = "Metriques de qualite")
    SonarMetricsDto metrics,

    @Schema(description = "Repartition des issues par severite")
    Map<String, Integer> issuesBySeverity,

    @Schema(description = "Repartition des issues par type")
    Map<String, Integer> issuesByType,

    @Schema(description = "Liste des issues (top 50)")
    List<SonarIssueDto> issues,

    @Schema(description = "Nombre total d'issues")
    int totalIssues,

    @Schema(description = "Nombre d'issues bloquantes ou critiques")
    int criticalIssuesCount
) {
    /**
     * Metriques de qualite SonarQube.
     */
    @Schema(description = "Metriques SonarQube")
    public record SonarMetricsDto(
        @Schema(description = "Dette technique en minutes")
        long technicalDebt,

        @Schema(description = "Dette technique formatee (ex: 2j 4h)")
        String technicalDebtFormatted,

        @Schema(description = "Ratio de dette technique (%)")
        double debtRatio,

        @Schema(description = "Note de maintenabilite (A-E)")
        String maintainabilityRating,

        @Schema(description = "Note de fiabilite (A-E)")
        String reliabilityRating,

        @Schema(description = "Note de securite (A-E)")
        String securityRating,

        @Schema(description = "Dette sur le nouveau code (minutes)")
        long newTechnicalDebt,

        @Schema(description = "Nombre de code smells")
        int codeSmells,

        @Schema(description = "Nombre de bugs")
        int bugs,

        @Schema(description = "Nombre de vulnerabilites")
        int vulnerabilities,

        @Schema(description = "Nombre de hotspots de securite")
        int securityHotspots,

        @Schema(description = "Hotspots de securite revus")
        int securityHotspotsReviewed,

        @Schema(description = "Couverture de code (%)")
        double coverage,

        @Schema(description = "Taux de duplication (%)")
        double duplications,

        @Schema(description = "Lignes de code")
        int linesOfCode
    ) {}

    /**
     * Issue SonarQube.
     */
    @Schema(description = "Issue SonarQube")
    public record SonarIssueDto(
        @Schema(description = "Identifiant unique")
        String key,

        @Schema(description = "Regle SonarQube")
        String rule,

        @Schema(description = "Severite (BLOCKER, CRITICAL, MAJOR, MINOR, INFO)")
        String severity,

        @Schema(description = "Type (BUG, VULNERABILITY, CODE_SMELL)")
        String type,

        @Schema(description = "Message de l'issue")
        String message,

        @Schema(description = "Fichier concerne")
        String component,

        @Schema(description = "Numero de ligne")
        int line,

        @Schema(description = "Effort de correction")
        String effort
    ) {}

    /**
     * Verifie si le quality gate est passe.
     */
    public boolean qualityGatePassed() {
        return "OK".equals(qualityGateStatus);
    }

    /**
     * Verifie si le projet a des issues critiques.
     */
    public boolean hasCriticalIssues() {
        return criticalIssuesCount > 0;
    }
}

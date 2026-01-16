package fr.cnam.reportui.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO pour l'analyse OSS Index (viabilité des dépendances).
 */
public record OssAnalysisDto(
    LocalDateTime analysisDate,
    int totalDependencies,
    double overallHealthScore,
    String overallStatus,
    double healthyPercentage,
    Map<String, Integer> healthDistribution,
    int outdatedCount,
    int vulnerableCount,
    int staleCount,
    List<OssDependencyDto> criticalAlerts,
    List<OssDependencyDto> dependencies
) {
    /**
     * DTO pour une dépendance avec ses métriques de santé.
     */
    public record OssDependencyDto(
        String groupId,
        String artifactId,
        String currentVersion,
        String latestVersion,
        long releaseAgeMonths,
        int majorVersionsBehind,
        int minorVersionsBehind,
        int patchVersionsBehind,
        int criticalVulns,
        int highVulns,
        int mediumVulns,
        int lowVulns,
        int totalVulnerabilities,
        double healthScore,
        String healthStatus,
        boolean hasUpdate
    ) {
        /**
         * Retourne les coordonnées Maven.
         */
        public String coordinates() {
            return groupId + ":" + artifactId + ":" + currentVersion;
        }

        /**
         * Retourne le nombre total de versions en retard.
         */
        public int totalVersionsBehind() {
            return majorVersionsBehind + minorVersionsBehind + patchVersionsBehind;
        }
    }
}

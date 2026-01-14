package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Statistiques agrégées d'un rapport de migration.
 */
@Schema(description = "Statistiques du rapport de migration")
public record StatisticsDto(
    @Schema(description = "Nombre total de JARs détectés", example = "85")
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

    @Schema(description = "Taux de succès en pourcentage (ancien calcul)", example = "84.7")
    double successRate,

    @Schema(description = "Taux de couverture en pourcentage (nouveau calcul)", example = "98.6")
    Double coverageRate,

    @Schema(description = "Répartition par portée Maven")
    Map<String, Integer> byScope,

    @Schema(description = "Répartition par méthode de résolution")
    Map<String, Integer> byMethod,

    @Schema(description = "Résolus depuis repos standards (hors migration-java-dette)")
    Integer resolvedStd,

    @Schema(description = "Résolus internes depuis migration-java-dette")
    Integer resolvedInt,

    @Schema(description = "Résolus externes depuis migration-java-dette")
    Integer resolvedExt,

    @Schema(description = "Indique si le repo migration-java-dette est utilisé")
    Boolean hasMigrationRepo
) {}

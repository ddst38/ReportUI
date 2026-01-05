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

    @Schema(description = "Taux de succès en pourcentage", example = "84.7")
    double successRate,

    @Schema(description = "Répartition par portée Maven")
    Map<String, Integer> byScope,

    @Schema(description = "Répartition par méthode de résolution")
    Map<String, Integer> byMethod
) {}

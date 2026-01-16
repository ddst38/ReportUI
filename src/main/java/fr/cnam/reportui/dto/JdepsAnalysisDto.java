package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Resultat complet de l'analyse jdeps.
 */
@Schema(description = "Analyse structurelle jdeps du projet")
public record JdepsAnalysisDto(
    @Schema(description = "Resume de l'analyse")
    JdepsSummaryDto summary,

    @Schema(description = "Dependances entre packages")
    List<PackageDependencyDto> packageDependencies,

    @Schema(description = "Cycles de dependances detectes")
    List<CycleDependencyDto> cycles,

    @Schema(description = "Usages d'APIs internes JDK")
    List<JdkInternalUsageDto> jdkInternalUsages,

    @Schema(description = "Metriques de couplage par package")
    List<PackageMetricsDto> packageMetrics
) {
    /**
     * Resume de l'analyse jdeps.
     */
    @Schema(description = "Resume des metriques jdeps")
    public record JdepsSummaryDto(
        @Schema(description = "Nombre total de packages analyses", example = "25")
        int totalPackages,

        @Schema(description = "Nombre total de dependances entre packages", example = "120")
        int totalDependencies,

        @Schema(description = "Nombre de cycles de dependances detectes", example = "3")
        int cycleCount,

        @Schema(description = "Nombre d'usages d'APIs internes JDK", example = "5")
        int jdkInternalCount,

        @Schema(description = "Instabilite moyenne des packages (0=stable, 1=instable)", example = "0.45")
        double avgInstability
    ) {}

    /**
     * Dependance entre deux packages.
     */
    @Schema(description = "Dependance entre packages")
    public record PackageDependencyDto(
        @Schema(description = "Package source", example = "fr.cnam.app.service")
        String sourcePackage,

        @Schema(description = "Package cible", example = "fr.cnam.app.repository")
        String targetPackage,

        @Schema(description = "Module JDK cible (si applicable)", example = "java.base")
        String targetModule
    ) {}

    /**
     * Cycle de dependances entre packages.
     */
    @Schema(description = "Cycle de dependances")
    public record CycleDependencyDto(
        @Schema(description = "Liste des packages formant le cycle")
        List<String> packages,

        @Schema(description = "Longueur du cycle", example = "3")
        int length,

        @Schema(description = "Representation textuelle du cycle", example = "a -> b -> c -> a")
        String cycleString
    ) {}

    /**
     * Usage d'une API interne JDK.
     */
    @Schema(description = "Usage d'API interne JDK")
    public record JdkInternalUsageDto(
        @Schema(description = "Classe source utilisant l'API", example = "fr.cnam.app.util.UnsafeHelper")
        String sourceClass,

        @Schema(description = "API interne utilisee", example = "sun.misc.Unsafe")
        String internalApi,

        @Schema(description = "Module JDK contenant l'API", example = "jdk.unsupported")
        String module,

        @Schema(description = "Suggestion de remplacement")
        String suggestion
    ) {}

    /**
     * Metriques de couplage d'un package.
     */
    @Schema(description = "Metriques de couplage")
    public record PackageMetricsDto(
        @Schema(description = "Nom du package", example = "fr.cnam.app.service")
        String packageName,

        @Schema(description = "Couplage afferent (Ca) - packages qui dependent de celui-ci", example = "5")
        int afferentCoupling,

        @Schema(description = "Couplage efferent (Ce) - packages dont celui-ci depend", example = "8")
        int efferentCoupling,

        @Schema(description = "Instabilite I = Ce/(Ca+Ce), 0=stable, 1=instable", example = "0.62")
        double instability,

        @Schema(description = "Liste des packages dont celui-ci depend")
        List<String> dependsOn,

        @Schema(description = "Liste des packages qui dependent de celui-ci")
        List<String> usedBy
    ) {}

    /**
     * Verifie si l'analyse a des cycles.
     */
    public boolean hasCycles() {
        return cycles != null && !cycles.isEmpty();
    }

    /**
     * Verifie si l'analyse a des usages d'APIs internes JDK.
     */
    public boolean hasJdkInternals() {
        return jdkInternalUsages != null && !jdkInternalUsages.isEmpty();
    }
}

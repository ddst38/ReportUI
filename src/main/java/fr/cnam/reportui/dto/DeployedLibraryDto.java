package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Représente une librairie déployée sur un repository Maven distant.
 */
@Schema(description = "Librairie déployée sur Artifactory/Nexus")
public record DeployedLibraryDto(
    @Schema(description = "GroupId Maven", example = "fr.cnamts.jk")
    String groupId,

    @Schema(description = "ArtifactId Maven", example = "jk-socle-core")
    String artifactId,

    @Schema(description = "Version Maven", example = "SHA-abc123def456")
    String version,

    @Schema(description = "Coordonnées GAV complètes", example = "fr.cnamts.jk:jk-socle-core:1.0.0")
    String gav,

    @Schema(description = "Nom du JAR original", example = "DEPFAB.jk-socle-core.jar")
    String originalJar,

    @Schema(description = "Type de librairie", example = "INTERNAL")
    String type,

    @Schema(description = "Taille en octets")
    long size
) {}

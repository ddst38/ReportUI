package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Informations sur une bibliothèque dans le rapport de migration.
 */
@Schema(description = "Informations sur une bibliothèque")
public record LibraryInfoDto(
    @Schema(description = "Nom original du fichier JAR", example = "DEPFAB.S8_J.commons-lang3-3.12.0.jar")
    String originalName,

    @Schema(description = "Nom nettoyé du fichier", example = "commons-lang3-3.12.0.jar")
    String cleanedName,

    @Schema(description = "GroupId Maven", example = "org.apache.commons")
    String groupId,

    @Schema(description = "ArtifactId Maven", example = "commons-lang3")
    String artifactId,

    @Schema(description = "Version Maven", example = "3.12.0")
    String version,

    @Schema(description = "Portée Maven", example = "COMPILE")
    String scope,

    @Schema(description = "Méthode de résolution", example = "CHECKSUM")
    String resolutionMethod,

    @Schema(description = "Taille en octets", example = "587402")
    long size,

    @Schema(description = "Hash SHA1 du fichier", example = "a1b2c3d4e5f6...")
    String sha1,

    @Schema(description = "Statut de résolution", example = "RESOLVED")
    String status,

    @Schema(description = "Bibliothèque interne CNAM", example = "false")
    boolean isInternal,

    @Schema(description = "Source d'identification: ARTIFACTORY, MAVEN_CENTRAL, CACHE", example = "MAVEN_CENTRAL")
    String identificationSource,

    @Schema(description = "Type de bibliothèque: internal ou external", example = "external")
    String libraryType,

    @Schema(description = "Ajoutée via --auto-fix", example = "false")
    boolean isAutoFixProvided,

    @Schema(description = "Installée localement (liblocale)", example = "false")
    boolean isLocalInstall,

    @Schema(description = "Repository source de la résolution", example = "maven-releases")
    String sourceRepository,

    @Schema(description = "Provient du repo de dette technique (migration-java-dette)", example = "false")
    Boolean isFromDebtRepo
) {
    /**
     * Retourne les coordonnées Maven au format GAV.
     */
    public String toGav() {
        return groupId + ":" + artifactId + ":" + version;
    }
}

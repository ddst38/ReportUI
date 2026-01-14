package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Informations sur le déploiement des librairies vers un repository Maven distant.
 */
@Schema(description = "Informations de déploiement vers Artifactory/Nexus")
public record DeploymentInfoDto(
    @Schema(description = "Type de repository", example = "NEXUS")
    String repositoryType,

    @Schema(description = "URL du repository", example = "https://nexus.company.com/repository/java-dette")
    String repositoryUrl,

    @Schema(description = "Nom du repository", example = "java-dette")
    String repositoryName,

    @Schema(description = "Liste des librairies déployées")
    List<DeployedLibraryDto> deployedLibraries,

    @Schema(description = "Nombre de librairies déployées")
    int deployedCount
) {}

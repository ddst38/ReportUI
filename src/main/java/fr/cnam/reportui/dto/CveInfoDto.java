package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Informations sur une vulnérabilité CVE.
 */
@Schema(description = "Vulnérabilité CVE détectée")
public record CveInfoDto(
    @Schema(description = "Identifiant CVE", example = "CVE-2024-12345")
    String cveId,

    @Schema(description = "Score CVSS v3", example = "9.8")
    double cvssScore,

    @Schema(description = "Niveau de sévérité", example = "CRITICAL")
    String severity,

    @Schema(description = "Couleur associée à la sévérité", example = "#dc2626")
    String severityColor,

    @Schema(description = "Description de la vulnérabilité")
    String description,

    @Schema(description = "Nom de la librairie affectée", example = "log4j-core-2.14.1.jar")
    String libraryName,

    @Schema(description = "Coordonnées Maven", example = "org.apache.logging.log4j:log4j-core:2.14.1")
    String gav,

    @Schema(description = "Identifiant CWE", example = "CWE-502")
    String cweId,

    @Schema(description = "URL de référence")
    String reference
) {
    /**
     * Retourne l'URL NVD pour cette CVE.
     */
    public String nvdUrl() {
        return "https://nvd.nist.gov/vuln/detail/" + cveId;
    }
}

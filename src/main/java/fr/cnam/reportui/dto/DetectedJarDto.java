package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Représente un fichier JAR détecté dans le projet source.
 */
@Schema(description = "Fichier JAR détecté dans le projet source avant traitement")
public record DetectedJarDto(
    @Schema(description = "Nom du fichier JAR", example = "commons-lang3-3.12.0.jar")
    String name,

    @Schema(description = "Taille en octets", example = "587402")
    long size,

    @Schema(description = "Source du JAR", example = "Répertoire lib")
    String source,

    @Schema(description = "Catégorie du JAR", example = "MAIN")
    String category,

    @Schema(description = "Tag du cadre si le JAR provient de libcadre", example = "PRF1190208A")
    String cadreTag
) {}

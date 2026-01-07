package fr.cnam.reportui.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Représente un package ou une classe manquante lors de la compilation.
 */
@Schema(description = "Package ou classe manquante détecté lors de la compilation")
public record MissingPackageDto(
    @Schema(description = "Type: PACKAGE ou CLASS", example = "PACKAGE")
    String type,

    @Schema(description = "Nom complet du package ou de la classe", example = "fr.cnamts.socle.util")
    String name,

    @Schema(description = "Nom du package extrait (pour regroupement)", example = "fr.cnamts.socle")
    String packageName,

    @Schema(description = "Fichier source où l'erreur a été détectée", example = "MyClass.java")
    String sourceFile
) {}

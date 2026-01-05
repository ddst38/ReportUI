package fr.cnam.reportui.model;

/**
 * Méthode de résolution d'une dépendance.
 */
public enum ResolutionMethod {
    INTERNAL_PATTERN("Pattern interne"),
    KNOWN_CONFIG("Configuration connue"),
    ARTIFACTORY_CHECKSUM("Checksum Artifactory"),
    ARTIFACTORY("Recherche Artifactory"),
    CHECKSUM("Checksum Maven Central"),
    MANIFEST("Analyse MANIFEST.MF"),
    PATTERN("Pattern de nom"),
    PACKAGE_ANALYSIS("Analyse des packages"),
    UNRESOLVED("Non résolu");

    private final String description;

    ResolutionMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package fr.cnam.reportui.model;

/**
 * Portée Maven d'une dépendance.
 */
public enum Scope {
    COMPILE("Compilation"),
    PROVIDED("Fourni"),
    RUNTIME("Exécution"),
    TEST("Test"),
    SYSTEM("Système");

    private final String description;

    Scope(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

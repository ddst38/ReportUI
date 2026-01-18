# report-ui - Architecture et contexte

## Vue d'ensemble

Interface web pour visualiser les rapports de migration ANT→Maven générés par ant2maven.
Reçoit les données via API REST (port 8090) et les stocke en fichiers JSON.

Supporte plusieurs types de migration : `ant2maven`, `toplink-to-jpa`, etc.

## Stack technique

- **Frontend:** Vue.js 3.4 + Vite 5 + Tailwind CSS 3.4 + Pinia 2.1 + Chart.js
- **Backend:** Spring Boot 3.2.1 + Java 21
- **Stockage:** Fichiers JSON dans `./data/reports/`

## Architecture en couches

```
ant2maven → POST /api/reports → Backend → JSON files → Frontend Vue.js
```

## Fichiers clés

### Frontend (Vue.js)

| Fichier | Responsabilité |
|---------|----------------|
| `frontend/src/components/ReportList.vue` | Liste des projets avec cards, stats et type migration |
| `frontend/src/components/ReportDetail.vue` | Vue détaillée avec stats colorées |
| `frontend/src/components/ReportIndicators.vue` | Icônes indicateurs (jdeps, Sonar, OSS, CVE...) |
| `frontend/src/components/LibraryTable.vue` | Tableau des bibliothèques (filtres, tri) |
| `frontend/src/components/VersionConflicts.vue` | Détection conflits de versions |
| `frontend/src/stores/reportStore.js` | Store Pinia (état global) |
| `frontend/src/api/reportApi.js` | Client Axios pour API |

### Backend (Spring Boot)

| Fichier | Responsabilité |
|---------|----------------|
| `controller/ReportController.java` | Endpoints REST CRUD |
| `service/ReportStorageService.java` | Persistance fichiers JSON |
| `dto/ProjectReportDto.java` | Rapport complet |
| `dto/LibraryInfoDto.java` | Infos bibliothèque |
| `dto/StatisticsDto.java` | Statistiques agrégées |

## Modèle de données

### LibraryInfoDto (données reçues de ant2maven)

```java
record LibraryInfoDto(
    String originalName,      // Nom JAR original
    String cleanedName,       // Nom nettoyé (sans DEPFAB.)
    String groupId,           // GroupId Maven
    String artifactId,        // ArtifactId Maven
    String version,           // Version Maven ou SHA-xxx
    String scope,             // COMPILE, PROVIDED, TEST, RUNTIME
    String resolutionMethod,  // CHECKSUM, ARTIFACTORY, INTERNAL_PATTERN, AUTO_FIX...
    long size,                // Taille en bytes
    String sha1,              // Hash SHA1
    String status,            // RESOLVED, LOCAL, UNRESOLVED
    boolean isInternal        // true si fr.cnam*
)
```

### StatisticsDto

```java
record StatisticsDto(
    int totalJars,
    int resolved,
    int unresolved,
    double successRate,
    Map<String, Integer> byScope,
    Map<String, Integer> byMethod
)
```

### ReportSummaryDto (liste des rapports)

```java
record ReportSummaryDto(
    String id,
    String projectName,
    String migrationType,        // ant2maven, toplink-to-jpa...
    LocalDateTime migrationDate,
    int totalJars,
    int resolved,
    int unresolved,
    double successRate,
    Boolean compilationSuccess,
    CveSummaryDto cveSummary,
    Boolean artifactoryEnabled,
    Boolean nexusEnabled,
    Integer jdepsDependencies,   // Nombre dépendances jdeps
    Integer sonarIssues,         // Nombre issues Sonar
    Integer ossAnalyzed          // Nombre dépendances OSS analysées
)
```

### Indicateurs visuels (ReportIndicators.vue)

| Prop | Type | Icône | Condition |
|------|------|-------|-----------|
| `autoFixEnabled` | Boolean | Engrenage violet | Si auto-fix activé |
| `deploymentMode` | String | Maison/Nuage | LOCAL/REMOTE |
| `compilationSuccess` | Boolean | Check/Warning | Compilation réussie/échouée |
| `cveSeverity` | String | Bouclier | CRITICAL/HIGH/MEDIUM/LOW |
| `artifactoryEnabled` | Boolean | Badge "A" | Artifactory utilisé |
| `nexusEnabled` | Boolean | Badge "N" | Nexus utilisé |
| `jdepsDependencies` | Number | Icône layers | Si > 0 |
| `sonarIssues` | Number | Badge "S" | Si présent |
| `ossAnalyzed` | Number | Bouclier check | Si > 0 |

### Couleurs stat boxes (ReportDetail.vue)

| Statistique | Background | Texte |
|-------------|------------|-------|
| JARs détectés | `bg-green-100` | `text-green-700` |
| Résolus | `bg-green-200` | `text-green-800` |
| Non rés. Interne | `bg-orange-100` | `text-orange-700` |
| Non rés. Externe | `bg-red-100` | `text-red-700` |
| Provided | `bg-purple-100` | `text-purple-700` |

## Commandes de développement

```bash
# Build complet
mvn clean package

# Exécuter
java -jar target/report-ui-1.0.0-SNAPSHOT.jar

# Frontend dev (avec hot-reload)
cd frontend && npm run dev

# Backend seul
mvn clean package -P skip-frontend
```

## Intégration ant2maven

ant2maven envoie les rapports via `ReportUiClient.java` :

```bash
java -jar ant2maven.jar \
  -p ./PROJECT \
  --auto-fix \
  --report-ui-url http://localhost:8090 \
  -v
```

## Conventions de code

- **Langue:** Français pour UI, commentaires en français
- **Composants Vue:** Composition API (`<script setup>`)
- **CSS:** Classes Tailwind (pas de CSS custom sauf nécessaire)
- **Backend:** Records Java pour DTOs

## Points d'attention

1. **Données ant2maven:** Le champ `isInternal` est calculé côté ant2maven (groupId.startsWith("fr.cnam"))
2. **Status:** Actuellement 3 valeurs (RESOLVED, LOCAL, UNRESOLVED) - LOCAL = version SHA installée localement
3. **Conflits versions:** Calculés côté frontend en groupant par groupId:artifactId
4. **Missing packages:** Disponibles uniquement si --auto-fix utilisé et échec compilation
5. **Type migration:** Champ `migrationType` affiché comme badge dans liste et détail
6. **Indicateurs analyses:** jdeps, Sonar, OSS affichés uniquement si données présentes

## Exemple ant2maven complet

Commande pour générer un rapport avec toutes les analyses :

```bash
java -jar ant2maven-1.0.0-SNAPSHOT.jar \
  -p ./PRF2_A \
  -o ./PRF2_A-maven \
  --nexus-url http://localhost:8084 \
  --nexus-user jenkins \
  --nexus-password jenkins \
  --deploy-mode REMOTE \
  --remote-target NEXUS \
  --deploy-repo migration-java-dette \
  --auto-fix \
  --known-artifacts ./conf-Ant2maven/known-artifacts.yaml \
  --report-ui-url http://localhost:8090 \
  --cve-check \
  --nvd-api-key votre-cle-nvd \
  --scan-cadre \
  --jdeps-analysis \
  --sonar-analysis \
  --sonar-url http://localhost:9000 \
  --sonar-token squ_xxxx \
  --oss-analysis \
  --ossindex-user user@example.com \
  --ossindex-token token-oss \
  -v
```

Cette commande enverra à report-ui un rapport complet avec :
- Type de migration `ant2maven`
- Statistiques de résolution
- Résultats CVE, jdeps, Sonar et OSS
- Informations de déploiement Nexus

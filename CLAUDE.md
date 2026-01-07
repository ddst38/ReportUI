# report-ui - Architecture et contexte

## Vue d'ensemble

Interface web pour visualiser les rapports de migration ANT→Maven générés par ant2maven.
Reçoit les données via API REST (port 8090) et les stocke en fichiers JSON.

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
| `frontend/src/components/ReportList.vue` | Liste des projets avec cards et stats |
| `frontend/src/components/ReportDetail.vue` | Vue détaillée d'un rapport |
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

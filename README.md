# Report-UI - Interface de visualisation des rapports de migration

Interface web centralisée pour visualiser les rapports de migration ANT vers Maven générés par **ant2maven**.

## Architecture

**Frontend:** Vue.js 3 + Vite + Tailwind CSS + Pinia
**Backend:** Spring Boot 3.2.1 (Java 21) + REST API
**Port:** 8090

## Fonctionnalités

### Gestion des rapports
- Réception des rapports de migration via API REST (POST `/api/reports`)
- Stockage persistant en fichiers JSON
- Suppression des rapports

### Liste des rapports
- Affichage en cards avec statistiques clés (JARs, résolus, non résolus)
- Badge du type de migration (ant2maven, toplink-to-jpa...)
- Indicateurs visuels pour les analyses effectuées :
  - **jdeps** : Icône bleue si analyse structurelle présente
  - **Sonar** : Badge "S" cyan si analyse qualité présente
  - **OSS** : Icône bouclier violet si analyse viabilité présente
- Indicateurs existants : compilation, CVE, Artifactory (A), Nexus (N), auto-fix

### Vue détaillée
- Statistiques colorées par catégorie :
  - JARs détectés : vert clair
  - Résolus : vert
  - Non résolus internes : orange
  - Non résolus externes : rouge
  - Provided : violet
- Graphiques interactifs (donut, barres, radar)
- Tableaux des bibliothèques avec filtres et tri
- Détection des conflits de versions
- Export PDF du rapport

## Structure du projet

```
report-ui/
├── pom.xml                          # Maven avec frontend-maven-plugin
├── frontend/                        # Application Vue.js
│   ├── src/
│   │   ├── components/              # Composants Vue
│   │   │   ├── ReportList.vue       # Liste des projets
│   │   │   ├── ReportDetail.vue     # Détails d'un rapport
│   │   │   ├── LibraryTable.vue     # Tableau des bibliothèques
│   │   │   ├── VersionConflicts.vue # Conflits de versions
│   │   │   └── charts/              # Graphiques (Donut, Bar, Radar)
│   │   ├── stores/reportStore.js    # Store Pinia (état)
│   │   ├── api/reportApi.js         # Client API
│   │   └── router/index.js          # Routes Vue
│   └── package.json
├── src/main/java/fr/cnam/reportui/
│   ├── controller/ReportController.java    # Endpoints REST
│   ├── service/ReportStorageService.java   # Stockage fichiers JSON
│   ├── dto/                                # DTOs
│   └── config/                             # Config CORS, OpenAPI
└── data/reports/                           # Stockage JSON (runtime)
```

## API REST

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/reports` | Créer un rapport (appelé par ant2maven) |
| GET | `/api/reports` | Lister tous les rapports |
| GET | `/api/reports/{id}` | Récupérer un rapport |
| DELETE | `/api/reports/{id}` | Supprimer un rapport |

Documentation Swagger : http://localhost:8090/swagger-ui.html

## Démarrage

### Build complet (frontend + backend)

```bash
mvn clean package
java -jar target/report-ui-1.0.0-SNAPSHOT.jar
```

### Développement frontend uniquement

```bash
cd frontend
npm install
npm run dev      # Dev server avec proxy vers backend
```

### Backend uniquement (skip frontend)

```bash
mvn clean package -P skip-frontend
java -jar target/report-ui-1.0.0-SNAPSHOT.jar
```

## Intégration avec ant2maven

Pour envoyer les rapports de migration à report-ui :

```bash
# Démarrer report-ui
java -jar report-ui-1.0.0-SNAPSHOT.jar

# Lancer ant2maven avec l'option --report-ui-url
java -jar ant2maven-1.0.0-SNAPSHOT.jar \
  -p ./MonProjet \
  --auto-fix \
  --report-ui-url http://localhost:8090 \
  -v
```

### Données envoyées par ant2maven

| Champ | Description |
|-------|-------------|
| `migrationType` | Type de migration (ant2maven, toplink-to-jpa...) |
| `statistics` | Statistiques de résolution (total, résolus, non résolus) |
| `cveSummary` | Résumé des vulnérabilités CVE (si `--cve-check`) |
| `jdepsAnalysis` | Résultat analyse jdeps (si `--jdeps-analysis`) |
| `sonarAnalysis` | Résultat analyse SonarQube (si `--sonar-analysis`) |
| `ossAnalysis` | Résultat analyse OSS Index (si `--oss-analysis`) |
| `deploymentInfo` | Informations de déploiement (mode, cible) |

### Indicateurs visuels

L'interface affiche des icônes pour indiquer les analyses effectuées :

| Indicateur | Condition d'affichage | Couleur |
|------------|----------------------|---------|
| Engrenage | Auto-fix activé | Violet |
| Maison | Mode LOCAL | Bleu |
| Nuage | Mode REMOTE | Bleu |
| Check vert | Compilation réussie | Vert |
| Warning | Compilation échouée | Rouge |
| Bouclier | CVE détectées | Rouge/Orange/Jaune selon sévérité |
| "A" | Artifactory utilisé | Orange |
| "N" | Nexus utilisé | Bleu |
| Flèche haut | Librairies uploadées | Vert |
| Layers | Analyse jdeps présente | Bleu |
| "S" | Analyse Sonar présente | Cyan |
| Bouclier check | Analyse OSS présente | Violet |

## Commandes de référence

### ant2maven avec export vers report-ui

```bash
# Migration standard avec auto-fix et export
java -jar ant2maven-1.0.0-SNAPSHOT.jar \
  -p /path/to/ANT_PROJECT \
  -o /path/to/maven-output \
  --auto-fix \
  --lib-provided /path/to/lib-provided \
  --report-ui-url http://localhost:8090 \
  -v

# Migration avec Artifactory
java -jar ant2maven-1.0.0-SNAPSHOT.jar \
  -p /path/to/ANT_PROJECT \
  --auto-fix \
  --artifactory-url https://artifactory.example.com/artifactory \
  --artifactory-cert /path/to/cert.crt \
  --report-ui-url http://localhost:8090 \
  -v
```

### Migration complète avec toutes les analyses

Cette commande active toutes les fonctionnalités et envoie le rapport complet à report-ui :

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

Avec cette commande, report-ui affichera :
- Badge "ant2maven" comme type de migration
- Icône jdeps (analyse structurelle active)
- Badge "S" (analyse SonarQube active)
- Icône OSS (analyse viabilité active)
- Statistiques CVE si vulnérabilités détectées
- Badge "N" (Nexus utilisé)
- Icône upload (librairies déployées en REMOTE)

### report-ui

```bash
# Build
mvn clean package

# Exécution
java -jar target/report-ui-1.0.0-SNAPSHOT.jar

# Développement frontend
cd frontend && npm run dev
```

## Configuration

Fichier `application.yml` :

```yaml
server:
  port: 8090

report-ui:
  storage-dir: ./data/reports   # Répertoire de stockage des rapports
```

## Technologies

| Composant | Technologie | Version |
|-----------|------------|---------|
| Frontend | Vue.js | 3.4.15 |
| Build | Vite | 5.0.11 |
| CSS | Tailwind CSS | 3.4.1 |
| State | Pinia | 2.1.7 |
| Backend | Spring Boot | 3.2.1 |
| Runtime | Java | 21 |
| API Docs | OpenAPI/Swagger | 2.3.0 |

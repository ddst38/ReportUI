# Report-UI - Interface de visualisation des rapports de migration

Interface web centralisée pour visualiser les rapports de migration ANT vers Maven générés par **ant2maven**.

## Architecture

**Frontend:** Vue.js 3 + Vite + Tailwind CSS + Pinia
**Backend:** Spring Boot 3.2.1 (Java 21) + REST API
**Port:** 8090

## Fonctionnalités

- Réception des rapports de migration via API REST (POST `/api/reports`)
- Visualisation en liste des projets migrés avec statistiques
- Vue détaillée par projet : graphiques, tableaux, conflits de versions
- Export PDF des rapports
- Suppression des rapports

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

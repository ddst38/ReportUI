package fr.cnam.reportui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cnam.reportui.dto.CreateReportRequest;
import fr.cnam.reportui.dto.ProjectReportDto;
import fr.cnam.reportui.dto.ReportSummaryDto;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * Service de stockage des rapports de migration en fichiers JSON.
 */
@Service
public class ReportStorageService {

    private static final Logger log = LoggerFactory.getLogger(ReportStorageService.class);

    private final Path storageDir;
    private final ObjectMapper objectMapper;

    public ReportStorageService(
            @Value("${report-ui.storage-dir:./data/reports}") String storageDir,
            ObjectMapper objectMapper) {
        this.storageDir = Path.of(storageDir);
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(storageDir);
        log.info("Répertoire de stockage des rapports: {}", storageDir.toAbsolutePath());
    }

    /**
     * Sauvegarde un nouveau rapport.
     */
    public ProjectReportDto save(CreateReportRequest request) throws IOException {
        String id = UUID.randomUUID().toString();
        LocalDateTime migrationDate = request.migrationDate() != null
            ? request.migrationDate()
            : LocalDateTime.now();

        ProjectReportDto report = new ProjectReportDto(
            id,
            request.projectName(),
            migrationDate,
            request.statistics(),
            request.libraries(),
            request.detectedJars(),
            request.missingPackages(),
            request.compilationSuccess(),
            request.cveVulnerabilities(),
            request.cveSummary(),
            request.deploymentInfo()
        );

        Path file = storageDir.resolve(id + ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), report);
        log.info("Rapport sauvegardé: {} ({})", request.projectName(), id);

        return report;
    }

    /**
     * Liste tous les rapports avec leurs résumés.
     */
    public List<ReportSummaryDto> listAll() {
        List<ReportSummaryDto> summaries = new ArrayList<>();

        try (Stream<Path> files = Files.list(storageDir)) {
            files.filter(p -> p.toString().endsWith(".json"))
                 .forEach(file -> {
                     try {
                         ProjectReportDto report = objectMapper.readValue(
                             file.toFile(), ProjectReportDto.class);
                         summaries.add(ReportSummaryDto.fromReport(report));
                     } catch (IOException e) {
                         log.warn("Erreur lecture fichier {}: {}", file, e.getMessage());
                     }
                 });
        } catch (IOException e) {
            log.error("Erreur listage répertoire: {}", e.getMessage());
        }

        // Tri par date décroissante
        summaries.sort((a, b) -> b.migrationDate().compareTo(a.migrationDate()));
        return summaries;
    }

    /**
     * Récupère un rapport par son ID.
     */
    public Optional<ProjectReportDto> findById(String id) {
        Path file = storageDir.resolve(id + ".json");
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(file.toFile(), ProjectReportDto.class));
        } catch (IOException e) {
            log.error("Erreur lecture rapport {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Supprime un rapport.
     */
    public boolean delete(String id) {
        Path file = storageDir.resolve(id + ".json");
        try {
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Erreur suppression rapport {}: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un rapport existe.
     */
    public boolean exists(String id) {
        return Files.exists(storageDir.resolve(id + ".json"));
    }
}

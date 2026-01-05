package fr.cnam.reportui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application principale ReportUI.
 * Interface centralisée pour visualiser les rapports de migration ant2maven.
 */
@SpringBootApplication
public class ReportUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportUiApplication.class, args);
    }
}

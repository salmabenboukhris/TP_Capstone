package com.example.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseMigrationTool {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseMigrationTool(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void executeMigration() {
        System.out.println("Démarrage de la migration de la base de données...");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("migration_v2.sql");
            if (inputStream == null) throw new RuntimeException("Script de migration non trouvé dans les ressources");
            String migrationScript = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            String[] instructions = migrationScript.split(";");
            try (Statement statement = connection.createStatement()) {
                for (String instruction : instructions) {
                    if (!instruction.trim().isEmpty()) {
                        System.out.println("Exécution: " + instruction.trim());
                        statement.execute(instruction);
                    }
                }
            }
            System.out.println("Migration terminée avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur lors de la migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DatabaseMigrationTool migrationTool = new DatabaseMigrationTool(
                "jdbc:mysql://localhost:3306/reservation_salles",
                "root",
                "password"
        );
        migrationTool.executeMigration();
    }
}

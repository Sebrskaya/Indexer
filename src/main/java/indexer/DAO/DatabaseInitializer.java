package indexer.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final String BASE_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DEFAULT_DB = "postgres";
    private static final String TARGET_DB = "indexer_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgre";

    // Создание бд, если ее еще нет
    public static void ensureDatabaseExists() {
        String adminUrl = BASE_URL + DEFAULT_DB;

        try (Connection conn = DriverManager.getConnection(adminUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Проверка на существование БД
            String checkSql = "SELECT 1 FROM pg_database WHERE datname = ?";
            try (var pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, TARGET_DB);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("База данных '" + TARGET_DB + "' уже существует.");
                        return;
                    }
                }
            }

            // Создание БД
            String createSql = "CREATE DATABASE " + TARGET_DB;
            stmt.execute(createSql);
            System.out.println("База данных '" + TARGET_DB + "' успешно создана");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
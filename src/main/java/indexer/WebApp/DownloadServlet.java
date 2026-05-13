package indexer.WebApp;

import indexer.DAO.IndexerDAO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadServlet extends HttpServlet {
    private final IndexerDAO indexerDAO = new IndexerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filePath = request.getParameter("path");

        if (filePath == null || filePath.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Не передан путь к файлу");
            return;
        }

        Path targetFile = Paths.get(filePath).toAbsolutePath().normalize();
        String normalizedPath = targetFile.toString();

        if (!indexerDAO.documentExists(normalizedPath)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Файл не найден в поисковом индексе");
            return;
        }

        if (!Files.isRegularFile(targetFile) || !Files.isReadable(targetFile)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Файл отсутствует на диске или недоступен для чтения");
            return;
        }

        String fileName = targetFile.getFileName().toString();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setContentLengthLong(Files.size(targetFile));

        try (OutputStream outputStream = response.getOutputStream()) {
            Files.copy(targetFile, outputStream);
        }
    }
}

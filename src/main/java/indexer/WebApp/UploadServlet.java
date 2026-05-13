package indexer.WebApp;

import indexer.IndexerCore.Indexer;
import indexer.DAO.IndexerDAO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class UploadServlet extends HttpServlet {

  private final Indexer textParser = new Indexer();
  private final IndexerDAO indexStorage = new IndexerDAO();
  private static final String UPLOAD_SUBDIR = "uploaded_files";

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=UTF-8");

    try {
      Part uploadedPart = request.getPart("file");
      String originalFileName = extractFileName(uploadedPart);

      if (!isPlainText(originalFileName)) {
        respondWithError(response, "Разрешены только файлы с расширением .txt");
        return;
      }

      // Идексирование и загрузка в бд
      Path destinationPath = saveUploadedFile(uploadedPart, originalFileName);
      Map<String, Integer> wordFrequencies = textParser.parseAndCount(destinationPath);
      Map<Path, Map<String, Integer>> batchIndex = Map.of(destinationPath, wordFrequencies);
      indexStorage.persistIndex(batchIndex);

      // Редирект обратно на поиск с флагом успешной загрузки
      String encodedName = java.net.URLEncoder.encode(originalFileName, java.nio.charset.StandardCharsets.UTF_8);
      response.sendRedirect("/search?uploaded=" + encodedName);

    } catch (Exception ex) {
      respondWithError(response, ex.getMessage());
    }
  }

  // Извлекает имя файла из Part
  private String extractFileName(Part part) {
    for (String content : part.getHeader("content-disposition").split(";")) {
      if (content.trim().startsWith("filename")) {
        return Paths.get(content.split("=")[1].trim().replace("\"", "")).getFileName().toString();
      }
    }
    return null;
  }

  // Проверка, что файл имеет расширение .txt
  private boolean isPlainText(String fileName) {
    return fileName != null && fileName.toLowerCase().endsWith(".txt");
  }

  // Сохраняет загруженный файл в директорию UPLOAD_SUBDIR
  private Path saveUploadedFile(Part part, String fileName) throws IOException {
    Path uploadRoot = Paths.get(System.getProperty("user.dir"), UPLOAD_SUBDIR);
    if (!Files.exists(uploadRoot)) {
      Files.createDirectories(uploadRoot);
    }
    Path targetPath = uploadRoot.resolve(fileName);
    try (var inputStream = part.getInputStream()) {
      Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
    return targetPath;
  }

  // Возвращает простую HTML-страницу с сообщением об ошибке и ссылкой на поиск
  private void respondWithError(HttpServletResponse resp, String message) throws IOException {
    resp.getWriter().println("""
            <!DOCTYPE html>
            <html lang="ru">
            <head><meta charset="UTF-8"><title>Ошибка</title></head>
            <body style="font-family:system-ui,sans-serif;max-width:600px;margin:2rem auto;padding:1rem;">
                <h2>Ошибка</h2>
                <p>%s</p>
                <a href="/search">Вернуться к поиску</a>
            </body>
            </html>
            """.formatted(escapeHtml(message)));
  }

  private String escapeHtml(String input) {
    if (input == null) return "";
    return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }
}
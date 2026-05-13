package indexer.WebApp;

import indexer.DAO.IndexerDAO;
import indexer.DAO.SearchResult;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {
    private final IndexerDAO indexerDAO = new IndexerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String searchQuery = request.getParameter("word");

        try (PrintWriter writer = response.getWriter()) {
            renderPageHeader(writer);
            renderSearchForm(writer, searchQuery);
            renderUploadSection(writer, request);

            if (searchQuery != null && !searchQuery.isBlank()) {
                List<SearchResult> matches = indexerDAO.findByWord(searchQuery.trim());
                renderResults(writer, matches, searchQuery);
            }

            renderPageFooter(writer);
        }
    }

    private void renderPageHeader(PrintWriter out) {
        out.println("""
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="UTF-8">
                <title>Поиск по индексу</title>
                <style>
                    body { font-family: system-ui, -apple-system, sans-serif; max-width: 800px; margin: 2rem auto; padding: 0 1rem; color: #333; }
                    h1 { margin-bottom: 0.5rem; }
                    form { margin: 1.5rem 0; display: flex; gap: 0.5rem; }
                    input[type="text"] {
                        flex: 1; padding: 0.75rem; font-size: 1rem;
                        border: 1px solid #ccc; border-radius: 6px;
                        transition: border-color 0.2s;
                    }
                    input[type="text"]:focus { outline: none; border-color: #0066cc; box-shadow: 0 0 0 2px rgba(0,102,204,0.2); }
                    table { width: 100%; border-collapse: collapse; margin: 1.5rem 0; }
                    th, td { padding: 0.75rem; border-bottom: 1px solid #eee; text-align: left; }
                    th { background: #f8f9fa; font-weight: 600; }
                    a { color: #0066cc; text-decoration: none; }
                    a:hover { text-decoration: underline; }
                    .empty { color: #666; font-style: italic; margin-top: 1rem; }
                    .success { background: #d1e7dd; color: #0f5132; padding: 0.75rem; border-radius: 6px; margin: 1rem 0; }
                    .upload-section { display: none; margin: 1.5rem 0; padding: 1rem; border: 1px dashed #ccc; border-radius: 6px; }
                    .upload-section.active { display: block; }
                    .toggle-btn { background: #f1f3f5; border: 1px solid #dee2e6; padding: 0.5rem 1rem; border-radius: 6px; cursor: pointer; }
                    .toggle-btn:hover { background: #e9ecef; }
                </style>
                <script>
                    function toggleUpload() {
                        document.getElementById('uploadSection').classList.toggle('active');
                    }
                </script>
            </head>
            <body>
            """);
    }

    private void renderSearchForm(PrintWriter out, String currentQuery) {
        out.println("<h1>Поиск по индексированным файлам</h1>");
        out.println("""
            <form method="get" action="/search">
                <input type="text" name="word" placeholder="Введите слово и нажмите Enter" value="%s" required>
            </form>
            <button type="button" class="toggle-btn" onclick="toggleUpload()">± Загрузить файл</button>
            """.formatted(currentQuery != null ? escapeHtml(currentQuery) : ""));
    }

    private void renderUploadSection(PrintWriter out, HttpServletRequest req) {
        out.println("""
            <div id="uploadSection" class="upload-section">
                <form method="post" action="/upload" enctype="multipart/form-data">
                    <p style="margin: 0 0 0.5rem;">Загрузите текстовый файл (.txt) для индексации:</p>
                    <input type="file" name="file" accept=".txt" required>
                    <button type="submit" class="toggle-btn">Загрузить</button>
                    <button type="button" class="toggle-btn" onclick="toggleUpload()">Отмена</button>
                </form>
            </div>
            """);

        String uploaded = req.getParameter("uploaded");
        if (uploaded != null) {
            out.println("<div class=\"success\">Файл <strong>" + escapeHtml(uploaded) + "</strong> загружен и проиндексирован</div>");
        }
    }

    private void renderResults(PrintWriter out, List<SearchResult> results, String query) {
        if (results.isEmpty()) {
            out.println("<p class=\"empty\">Ничего не найдено по запросу: <strong>" + escapeHtml(query) + "</strong></p>");
            return;
        }

        out.println("""
            <table>
                <thead><tr><th>Файл</th><th style="width:100px">Частота</th></tr></thead>
                <tbody>
            """);

        for (SearchResult item : results) {
            String encodedPath = URLEncoder.encode(item.filePath(), StandardCharsets.UTF_8);
            out.printf("""
                <tr>
                    <td><a href="/download?path=%s">%s</a></td>
                    <td>%d</td>
                </tr>
                """, encodedPath, escapeHtml(item.fileName()), item.frequency());
        }

        out.println("</tbody></table>");
    }

    private void renderPageFooter(PrintWriter out) {
        out.println("</body></html>");
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
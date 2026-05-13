package indexer.DAO;


// Для передачи результатов поиска из DAO в веб-слой
public record SearchResult(String filePath, String fileName, int frequency) {}

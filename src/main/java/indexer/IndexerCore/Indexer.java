package indexer.IndexerCore;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class Indexer {
    // Паттерн, оставляющий только слова и цифры
    private static final Pattern SEPARATOR = Pattern.compile("[^\\p{L}\\p{N}]+");

    // Считывание файла и разбиение на слова с помощью паттерна
    public Map<String, Integer> parseAndCount(Path targetFile) throws IOException {
        Map<String, Integer> wordStats = new ConcurrentHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(targetFile, StandardCharsets.UTF_8)) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] tokens = SEPARATOR.split(currentLine);
                for (String token : tokens) {
                    if (token.isEmpty()) {
                        continue;
                    }
                    String normalized = token.toLowerCase();
                    wordStats.merge(normalized, 1, Integer::sum);
                }
            }
        }
        return wordStats;
    }
}
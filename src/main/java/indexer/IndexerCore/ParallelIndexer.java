package indexer.IndexerCore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ParallelIndexer {
    private final Indexer wordProcessor = new Indexer();
    // Мапа - путь к файлу, карта частотности слов
    private final Map<Path, Map<String, Integer>> documentStats = new ConcurrentHashMap<>();
    // Успешно обработанные файлы
    private final List<Path> indexedPaths = Collections.synchronizedList(new ArrayList<>());

    // рекурсивно обходит директорию и индексирует все .txt файлы в параллели
    public void indexDirectory(Path rootDirectory, int poolSize) throws IOException {
        if (!Files.isDirectory(rootDirectory)) {
            throw new IllegalArgumentException("Указанный путь не является директорией: " + rootDirectory);
        }

        List<Path> targetFiles;
        try (Stream<Path> pathStream = Files.walk(rootDirectory)) {
            targetFiles = pathStream.filter(Files::isRegularFile).filter(this::hasTxtExtension).toList();
        }

        if (targetFiles.isEmpty()) {
            System.out.println("Текстовые файлы не найдены в указанной директории");
            return;
        }

        System.out.printf("Найдено файлов: %d. Запуск %d потоков%n", targetFiles.size(), poolSize);

        List<Callable<Void>> indexingTasks = buildTasks(targetFiles);

        try (ExecutorService executor = Executors.newFixedThreadPool(poolSize)) {
            executor.invokeAll(indexingTasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Процесс индексации был прерван", e);
        }

        System.out.println("Индексация завершена");
        System.out.printf("Успешно обработано файлов: %d%n", indexedPaths.size());
    }

    // Формирование списка задач для каждого файла
    private List<Callable<Void>> buildTasks(List<Path> files) {
        List<Callable<Void>> taskList = new ArrayList<>();
        for (Path currentFile : files) {
            taskList.add(() -> processSingleFile(currentFile));
        }
        return taskList;
    }

    // Индексация одного файла в потоке
    private Void processSingleFile(Path filePath) {
        try {
            Map<String, Integer> wordCounts = wordProcessor.parseAndCount(filePath);
            documentStats.put(filePath, wordCounts);
            indexedPaths.add(filePath);
        } catch (IOException ex) {
            System.err.println("Ошибка при обработке файла " + filePath + ": " + ex.getMessage());
        }
        return null;
    }

    public Map<Path, Map<String, Integer>> getDocumentIndex() {
        return documentStats;
    }

    private boolean hasTxtExtension(Path path) {
        return path.toString().toLowerCase().endsWith(".txt");
    }
}
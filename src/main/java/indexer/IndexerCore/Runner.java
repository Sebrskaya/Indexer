package indexer.IndexerCore;

import indexer.DAO.IndexerDAO;
import java.nio.file.Path;

public class Runner {

    public static void main(String[] args) {
        if (args.length == 0 || args[0].isBlank()) {
            System.out.println("Укажите путь к директории для индексации файлов");
            System.out.println("Использование: java -jar Indexer.jar <путь_к_директории>");
            return;
        }
        Path targetDirectory = Path.of(args[0]).toAbsolutePath();
        int maxThreads = Runtime.getRuntime().availableProcessors();

        IndexerDAO indexerDAO = null;
        try {
            ParallelIndexer parallelIndexer = new ParallelIndexer();
            parallelIndexer.indexDirectory(targetDirectory, maxThreads);

            indexerDAO = new IndexerDAO();
            indexerDAO.persistIndex(parallelIndexer.getDocumentIndex());

            System.out.println("Индекс успешно сохранён в базу данных PostgreSQL.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            if (indexerDAO != null) {
                indexerDAO.close();
            }
        }
    }
}

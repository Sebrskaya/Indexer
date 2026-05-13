package indexer.DAO;

import indexer.Entities.Document;
import indexer.Entities.Word;
import indexer.Entities.WordFrequency;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexerDAO {
    private final EntityManagerFactory entityManagerFactory;

    public IndexerDAO() {
        DatabaseInitializer.ensureDatabaseExists();
        this.entityManagerFactory = Persistence.createEntityManagerFactory("indexer");
    }

    // Сохранение рассчитанных частот слов в PostgreSQL
    public void persistIndex(Map<Path, Map<String, Integer>> documentIndexMap) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();

            Map<String, Long> documentIdLookup = new HashMap<>();
            Map<String, Long> wordIdLookup = new HashMap<>();

            // Предзагрузка существующих идентификаторов документов
            List<Object[]> existingDocuments = entityManager.createQuery(
                    "SELECT d.filePath, d.id FROM Document d", Object[].class).getResultList();
            existingDocuments.forEach(row -> documentIdLookup.put((String) row[0], (Long) row[1]));

            // Предзагрузка существующих идентификаторов слов
            List<Object[]> existingWords = entityManager.createQuery(
                    "SELECT w.text, w.id FROM Word w", Object[].class).getResultList();
            existingWords.forEach(row -> wordIdLookup.put((String) row[0], (Long) row[1]));

            final int flushThreshold = 500;
            int operationCounter = 0;

            // цикл по документам
            for (Map.Entry<Path, Map<String, Integer>> docEntry : documentIndexMap.entrySet()) {
                Path currentPath = docEntry.getKey();
                String absolutePath = currentPath.toAbsolutePath().toString();
                Long currentDocId = documentIdLookup.get(absolutePath);

                // документа нет - добавление в бд
                if (currentDocId == null) {
                    Document newDoc = new Document(absolutePath, currentPath.getFileName().toString());
                    entityManager.persist(newDoc);
                    entityManager.flush();
                    currentDocId = newDoc.getId();
                    documentIdLookup.put(absolutePath, currentDocId);
                } else {
                    // Документ уже есть, очищаем старые частоты перед перезаписью
                    entityManager.createQuery("DELETE FROM WordFrequency f WHERE f.document.id = :docId")
                            .setParameter("docId", currentDocId).executeUpdate();
                }

                // цикл по словам в документа
                for (Map.Entry<String, Integer> wordEntry : docEntry.getValue().entrySet()) {
                    String currentWord = wordEntry.getKey();
                    Long currentWordId = wordIdLookup.get(currentWord);

                    // если слова нет - создание
                    if (currentWordId == null) {
                        Word newWord = new Word(currentWord);
                        entityManager.persist(newWord);
                        entityManager.flush();
                        currentWordId = newWord.getId();
                        wordIdLookup.put(currentWord, currentWordId);
                    }

                    // Вставка объекта WordFrequency в бд
                    Document docRef = entityManager.getReference(Document.class, currentDocId);
                    Word wordRef = entityManager.getReference(Word.class, currentWordId);
                    entityManager.persist(new WordFrequency(docRef, wordRef, wordEntry.getValue()));

                    if (++operationCounter % flushThreshold == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                }
            }
            transaction.commit();
        } catch (Exception exception) {
            throw new RuntimeException("Ошибка сохранения поискового индекса в БД", exception);
        }
    }

    // Поиск документов по слову. Возвращает список, отсортированный по убыванию
    // частоты
    public List<SearchResult> findByWord(String searchTerm) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            String jpql = """
                    SELECT new indexer.DAO.SearchResult(d.filePath, d.fileName, wf.count)
                    FROM WordFrequency wf
                    JOIN wf.document d
                    JOIN wf.word w
                    WHERE w.text = :targetWord
                    ORDER BY wf.count DESC
                    """;
            TypedQuery<SearchResult> query = entityManager.createQuery(jpql, SearchResult.class);
            query.setParameter("targetWord", searchTerm.toLowerCase());
            return query.getResultList();
        }
    }

    // Проверка, что документ есть в индексе. Используется перед скачиванием файла
    public boolean documentExists(String filePath) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            Long count = entityManager.createQuery(
                    "SELECT COUNT(d) FROM Document d WHERE d.filePath = :filePath", Long.class)
                    .setParameter("filePath", filePath)
                    .getSingleResult();
            return count > 0;
        }
    }

    // закрытие соединения с бд
    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
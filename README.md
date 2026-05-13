# Indexer

Java-приложение для полнотекстового индексирования и поиска с сохранением в PostgreSQL и встроенным веб-интерфейсом на Tomcat.

## Описание

Проект строит индекс текстовых файлов, сохраняет информацию о документах, словах и частотах в PostgreSQL и предоставляет веб-интерфейс для поиска и загрузки новых файлов.

## Структура проекта

- `src/main/java/indexer/IndexerCore/Runner.java` — CLI-утилита для индексирования директории и сохранения индекса в БД.
- `src/main/java/indexer/WebApp/WebApp.java` — встроенный Tomcat-сервер с веб-интерфейсом поиска.
- `src/main/java/indexer/WebApp/SearchServlet.java` — servlet для поиска по индексу.
- `src/main/java/indexer/WebApp/UploadServlet.java` — servlet для загрузки и индексирования новых `.txt` файлов.
- `src/main/resources/META-INF/persistence.xml` — конфигурация JPA / Hibernate и PostgreSQL.

## Требования

- Java 25
- Maven
- PostgreSQL

## Настройка PostgreSQL

1. Запустите PostgreSQL.
2. Убедитесь, что параметры подключения в `src/main/resources/META-INF/persistence.xml` настроены верно:

- `jakarta.persistence.jdbc.url` = `jdbc:postgresql://localhost:5432/indexer_db`
- `jakarta.persistence.jdbc.user` = `postgres`
- `jakarta.persistence.jdbc.password` = `postgres`

При первом запуске `Runner` база данных и необходимые таблицы создаются автоматически, если они ещё не существовали.

## Сборка

```bash
mvn clean package
```

## Запуск индексатора

```bash
java -jar target/Indexer-1.0-SNAPSHOT.jar <путь_к_директории>
```

Пример:

```bash
java -jar target/Indexer-1.0-SNAPSHOT.jar txt_files
```

После выполнения индекс будет сохранен в PostgreSQL.

## Запуск веб-приложения

В проекте используется встроенный Tomcat. Запустите класс `indexer.WebApp.WebApp` из IDE или настройте запуск из Maven.

После старта откройте в браузере:

```
http://localhost:8080/search
```

## Возможности веб-интерфейса

- Поиск по словам, найденным в индексированных файлах.
- Загрузка новых `.txt` файлов для индексирования через форму.
- Сохранение загруженных файлов в локальную папку `uploaded_files`.
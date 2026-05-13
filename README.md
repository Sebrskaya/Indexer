# Indexer — поисковик по текстовым файлам

Indexer — это учебное Java-приложение для индексации `.txt` файлов и поиска документов по частоте встречаемости слов.

Проект реализует сквозную лабораторную работу:  
от подсчёта слов в текстовых файлах до веб-интерфейса поиска с сохранением индекса в PostgreSQL.

## Возможности

- Рекурсивный обход папки с текстовыми файлами.
- Индексация всех `.txt` файлов из указанного каталога.
- Подсчёт частоты каждого слова в каждом документе.
- Параллельная обработка файлов в несколько потоков.
- Сохранение индекса в PostgreSQL.
- Веб-интерфейс для поиска по словам.
- Сортировка результатов по релевантности: чем чаще слово встречается в файле, тем выше файл в выдаче.
- Загрузка новых `.txt` файлов через веб-интерфейс.
- Скачивание найденных файлов из результатов поиска.
- Запуск всей системы через исполняемый `.jar` файл.

## Технологии

- Java 25+
- Maven
- PostgreSQL
- Hibernate ORM / JPA
- Embedded Tomcat
- Jakarta Servlet API

## Требования

Перед запуском должны быть установлены:

- JDK 25 или новее
- Maven
- PostgreSQL

Проверить версии можно командами:

```powershell
java -version
mvn -version
```

Если Maven использует современную Java, а обычная команда `java` показывает Java 8, запускайте приложение напрямую через нужный JDK, например:

```powershell
& "C:\Program Files\Java\jdk-26\bin\java.exe" -jar target/Indexer-1.0-SNAPSHOT.jar txt_files
```

## Настройка PostgreSQL

По умолчанию приложение подключается к PostgreSQL со следующими параметрами:

```text
host: localhost
port: 5432
database: indexer_db
user: postgres
password: postgres
```

Настройки находятся в файлах:

```text
src/main/resources/META-INF/persistence.xml
src/main/java/indexer/DAO/DatabaseInitializer.java
```

Если у вашего пользователя PostgreSQL другой пароль, измените его в обоих файлах.

База данных `indexer_db` создаётся автоматически при первом запуске индексатора.

## Сборка проекта

В корне проекта выполните:

```powershell
mvn clean package
```

После успешной сборки появится файл:

```text
target/Indexer-1.0-SNAPSHOT.jar
```

## Запуск приложения

### Полный запуск

Команда индексирует папку с файлами и запускает веб-приложение:

```powershell
java -jar target/Indexer-1.0-SNAPSHOT.jar txt_files
```

Если обычная команда `java` указывает на старую Java, используйте полный путь к JDK:

```powershell
& "C:\Program Files\Java\jdk-26\bin\java.exe" -jar target/Indexer-1.0-SNAPSHOT.jar txt_files
```

После запуска откройте в браузере:

```text
http://localhost:8080/search
```

### Только индексация

```powershell
java -jar target/Indexer-1.0-SNAPSHOT.jar --index txt_files
```

### Только веб-приложение

```powershell
java -jar target/Indexer-1.0-SNAPSHOT.jar --web
```

### Справка

```powershell
java -jar target/Indexer-1.0-SNAPSHOT.jar --help
```

## Как пользоваться

1. Поместите `.txt` файлы в папку `txt_files`.
2. Соберите проект командой:

```powershell
mvn clean package
```

3. Запустите приложение:

```powershell
java -jar target/Indexer-1.0-SNAPSHOT.jar txt_files
```

4. Откройте страницу:

```text
http://localhost:8080/search
```

5. Введите слово для поиска.
6. Приложение покажет список файлов, где найдено слово.
7. Файлы будут отсортированы по частоте встречаемости слова.
8. Нажмите на название файла, чтобы скачать его.

## Структура проекта

```text
src/main/java/indexer
├── App.java                         # Общая точка входа для запуска jar
├── DAO
│   ├── DatabaseInitializer.java      # Создание базы данных
│   └── IndexerDAO.java              # Работа с PostgreSQL
├── Entities
│   ├── Document.java                 # Сущность документа
│   ├── Word.java                     # Сущность слова
│   └── WordFrequency.java            # Частота слова в документе
├── IndexerCore
│   ├── Indexer.java                  # Подсчёт слов в одном файле
│   ├── ParallelIndexer.java          # Параллельная индексация каталога
│   ├── Runner.java                   # Консольный запуск индексации
│   └── SearchResult.java             # Результат поиска
└── WebApp
    ├── WebApp.java                   # Запуск Embedded Tomcat
    ├── SearchServlet.java            # Страница поиска
    ├── UploadServlet.java            # Загрузка новых файлов
    └── DownloadServlet.java          # Скачивание найденных файлов
```

## Принцип работы

Индексатор проходит по указанной папке, находит все `.txt` файлы и для каждого файла считает количество вхождений каждого слова.

Результат сохраняется в PostgreSQL в виде трёх основных сущностей:

- `Document` — информация о файле;
- `Word` — уникальное слово;
- `WordFrequency` — количество повторений слова в конкретном файле.

При поиске приложение находит документы, где встречается введённое слово, и сортирует их по убыванию частоты.

## Пример

Если в базе есть файлы:

```text
war_and_peace.txt
dubrovsky.txt
igor_tale.txt
```

и пользователь вводит:

```text
князь
```

приложение покажет файлы, где найдено слово `князь`, начиная с файла, где оно встречается чаще всего.

## Ограничения текущей версии

- Поиск работает по точному совпадению слова.
- Морфология не учитывается: `князь`, `князя`, `князю` считаются разными словами.
- Поиск по части слова не поддерживается.
- Поиск по нескольким словам может требовать отдельной доработки в зависимости от версии проекта.

## Возможные ошибки

### `no main manifest attribute`

Jar собран без указания главного класса. Нужно проверить настройку `maven-shade-plugin` в `pom.xml` и пересобрать проект:

```powershell
mvn clean package
```

### `Invalid signature file digest for Manifest main attributes`

В итоговый jar попали подписи зависимостей. В `maven-shade-plugin` нужно исключить:

```xml
<exclude>META-INF/*.SF</exclude>
<exclude>META-INF/*.DSA</exclude>
<exclude>META-INF/*.RSA</exclude>
```

### `password authentication failed for user "postgres"`

Неверный пароль PostgreSQL. Нужно изменить пароль в:

```text
src/main/resources/META-INF/persistence.xml
src/main/java/indexer/DAO/DatabaseInitializer.java
```

### `UnsupportedClassVersionError`

Проект собран новой Java, а запускается старой. Проверьте:

```powershell
java -version
mvn -version
```

Если `java -version` показывает Java 8, запускайте jar через полный путь к современному JDK.

## Автор

Egor Istomin

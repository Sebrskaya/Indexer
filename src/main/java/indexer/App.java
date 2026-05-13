package indexer;

import indexer.IndexerCore.Runner;
import indexer.WebApp.WebApp;

import java.util.Arrays;

public class App {
    public static void main(String[] args) {
        if (args.length > 0 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            printHelp();
            return;
        }

        if (args.length > 0 && "--index".equals(args[0])) {
            Runner.main(Arrays.copyOfRange(args, 1, args.length));
            return;
        }

        if (args.length > 0 && "--web".equals(args[0])) {
            WebApp.main(new String[0]);
            return;
        }

        if (args.length > 0 && !args[0].isBlank()) {
            Runner.main(new String[]{args[0]});
        }

        WebApp.main(new String[0]);
    }

    private static void printHelp() {
        System.out.println("Indexer — поисковик по текстовым файлам");
        System.out.println();
        System.out.println("Команды:");
        System.out.println("  java -jar target/Indexer-1.0-SNAPSHOT.jar txt_files");
        System.out.println("      Проиндексировать папку txt_files и запустить веб-приложение");
        System.out.println();
        System.out.println("  java -jar target/Indexer-1.0-SNAPSHOT.jar --index txt_files");
        System.out.println("      Только проиндексировать папку txt_files");
        System.out.println();
        System.out.println("  java -jar target/Indexer-1.0-SNAPSHOT.jar --web");
        System.out.println("      Только запустить веб-приложение");
        System.out.println();
        System.out.println("После запуска веб-приложения откройте: http://localhost:8080/search");
    }
}

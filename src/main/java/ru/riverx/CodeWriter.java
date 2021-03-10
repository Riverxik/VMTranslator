package ru.riverx;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CodeWriter {
    private final String filename;
    private List<String> lines;
    private final List<String> code;

    public CodeWriter(String filename) {
        this.filename = filename;
        this.lines = new ArrayList<>();
        this.code = new ArrayList<>();
        if (filename.contains(".vm"))
            translateFile();
        else
            translateDir();
    }

    public static void showErrorMessage() {
        System.out.println("Please provide the .vm file or directory to translate.");
        System.out.println("Example: java VMTranslator test.vm");
        System.out.println("Example: java VMTranslator test-folder");
    }

    private void translateFile() {
        lines = readFile(filename);
        Parser parser = new Parser(lines, filename, false);
        String name = filename.substring(0, filename.indexOf("."));
        writeFile(name, parser.code);
    }

    private void translateDir() {
        boolean init = true;
        List<String> filenames = getAllFilenames();
        if (filenames != null) {
            for (String file : filenames) {
                lines = readFile(file);
                Parser parser = new Parser(lines, filename, init);
                init = false;
                code.addAll(parser.code);
            }
            writeFile(filename, code);
        } else {
            System.out.println("No .vm files has been found in folder: " + filename);
            showErrorMessage();
        }
    }

    private List<String> readFile(String filename) {
        List<String> tmp = new ArrayList<String>();
        try {
            tmp = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error: " + e);
            e.printStackTrace();
        }
        return tmp;
    }

    private void writeFile(String filename, List<String> codeLines) {
        try {
            Files.write(Paths.get(filename + ".asm"), codeLines, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getAllFilenames() {
        try (Stream<Path> paths = Files.walk(Paths.get(filename))){
            List<String> files = new ArrayList<>();
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".vm"))
                    .forEach(path -> {
                        String base = path.getFileName().toString();
                        files.add(filename+"/"+base);
                    });
            return files;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package ru.riverx;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RiVeRx on 09.03.2021.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].contains(".vm")) {
                List<String> lines = readFile(args[0]);
                Parser parser = new Parser(lines, args[0]);
                parser.parse();
                writeFile(args[0], parser.code);
            } else {
                System.out.println("Wrong file format, please provide the .vm file");
            }
        } else {
            System.out.println("Please provide the .vm file to translate.");
            System.out.println("Example: java VMTranslator test.vm");
        }
    }

    private static List<String> readFile(String filename) {
        List<String> tmp = new ArrayList<String>();
        try {
            tmp = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error: " + e);
            e.printStackTrace();
        }
        return tmp;
    }

    private static void writeFile(String filename, List<String> lines) {
        try {
            String name = filename.substring(0, filename.indexOf("."));
            // it will override existing file if so (cause i want)
            Files.write(Paths.get(name + ".asm"), lines, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

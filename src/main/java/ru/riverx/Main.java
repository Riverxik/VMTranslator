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
            CodeWriter codeWriter = new CodeWriter(args[0]);
        } else {
            CodeWriter.showErrorMessage();
        }
    }
}

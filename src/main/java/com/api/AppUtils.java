package com.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AppUtils {

    private AppUtils() {
    }

    /*
     * Start the OCR process using ExtractTables which is a paid API
     * This method is used to start the OCR process
     * It returns a Process object
     */
    static Process startOCR() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python3",
                AppUtils.resolveResourcePathToString("OCR.py"));
        return processBuilder.start();
    }

    static Process startNLP(String message) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python3",
                AppUtils.resolveResourcePathToString("GPT4FREE.py"),
                message);
        //processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    static String fixJson(String json) {
        try {
            json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
            String trimmedJson = json.replaceAll("\\s+", "");
            String first = IntStream.range(0, trimmedJson.length())
                    .filter(i -> trimmedJson.charAt(i) != ',' || trimmedJson.charAt(i + 1) == '\"')
                    .mapToObj(i -> Character.toString(trimmedJson.charAt(i)))
                    .collect(Collectors.joining(""));
            String second = IntStream.range(0, first.length())
                    .filter(i -> first.charAt(i) != ',' || first.charAt(i - 1) == '\"' || first.charAt(i - 1) == ']'
                            || Character.isDigit(first.charAt(i - 1)))
                    .mapToObj(i -> Character.toString(first.charAt(i)))
                    .collect(Collectors.joining(""));
            return second.replace("null", "\"\",");
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON format");
        }
    }

    static List<String> readProcessOutput(InputStream inputStream)
            throws IOException {
        try (
                BufferedReader output = new BufferedReader(
                        new InputStreamReader(inputStream))) {
            return output.lines().collect(Collectors.toList());
        }
    }

    static Path resolveResourcePath(String filename) {
        File file = new File("src/main/resources/" + filename);
        return file.toPath();
    }

    static String resolveResourcePathToString(String filename) {
        File file = new File("src/main/resources/" + filename);
        return file.getAbsolutePath();
    }
}

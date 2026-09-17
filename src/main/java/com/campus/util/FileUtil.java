package com.campus.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * FileUtil — Utility methods for file/directory creation and writing.
 *
 * All report/log/backup output goes through this class so File I/O is
 * centralised rather than scattered across service and DAO classes.
 */
public class FileUtil {

    /**
     * Creates a directory (and all parents) if it does not already exist.
     */
    public static void createDirIfNeeded(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Writes content to a file, OVERWRITING any previous content.
     * Creates parent directories automatically.
     *
     * @param filePath  target file path
     * @param content   text to write
     */
    public static void writeToFile(String filePath, String content) {
        createDirIfNeeded(new File(filePath).getParent());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            bw.write(content);
            Logger.info("Report written to: " + filePath);
        } catch (IOException e) {
            Logger.error("Failed to write file: " + filePath, e);
            throw new RuntimeException("Could not write to file: " + filePath, e);
        }
    }

    /**
     * Appends content to a file.
     * Creates parent directories automatically.
     */
    public static void appendToFile(String filePath, String content) {
        createDirIfNeeded(new File(filePath).getParent());
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, true))) {
            pw.println(content);
        } catch (IOException e) {
            Logger.error("Failed to append to file: " + filePath, e);
        }
    }
}

package ru.sfedu.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    private static final Logger log = LogManager.getLogger(FileUtil.class.getName());

    public static void createFileIfNotExists(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            boolean flag = file.createNewFile();
            log.info("createFileIfNotExists [1]: New file {}, is created: {}", file.getAbsolutePath(), flag);
        } else {
            log.info("createFileIfNotExists [2]: File {} is exists", file.getAbsolutePath());
        }
    }

    public static void createFolderIfNotExists(String folderPath) throws IOException {
        File file = new File(folderPath);
        if (!file.exists()) {
            boolean flag = file.mkdirs();
            log.info("createFolderIfNotExists [1]: New folder {}, is created: {}", file.getAbsolutePath(), flag);
        } else {
            log.info("createFolderIfNotExists [2]: Folder {} is exists", file.getAbsolutePath());
        }
    }

    public static void deleteFileOrFolderIfExists(String folderPath) {
        File file = new File(folderPath);
        if (file.exists()) {
            boolean flag = file.delete();
            log.info("deleteFileOrFolderIfExists [1]: Folder of file {}, is deleted: {}", file.getAbsolutePath(), flag);
        } else {
            log.info("deleteFileOrFolderIfExists [2]: Folder or file {} is not exists", file.getAbsolutePath());
        }
    }

    public void reCreateFile(String filePath) {
        deleteFileOrFolderIfExists(filePath);
        try {
            createFileIfNotExists(filePath);
        } catch (IOException e) {
            log.error("reCreateFile [1]: - reCreateFile error {}", e.getMessage());
        }
    }
}

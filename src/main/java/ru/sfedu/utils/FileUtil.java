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
            log.info("saveModifySubject [1] : New file {}, is created: {}", file.getAbsolutePath(), flag);
        } else {
            log.info("saveModifySubject [2] : File {} is exists", file.getAbsolutePath());
        }
    }

    public static void createFolderIfNotExists(String folderPath) throws IOException {
        File file = new File(folderPath);
        if (!file.exists()) {
            boolean flag = file.mkdirs();
            log.info("saveModifySubject [1] : New folder {}, is created: {}", file.getAbsolutePath(), flag);
        } else {
            log.info("saveModifySubject [2] : Folder {} is exists", file.getAbsolutePath());
        }
    }
}

package ru.sfedu.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.Constants;
import ru.sfedu.model.Subject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static ru.sfedu.utils.FileUtil.createFileIfNotExists;

public class CsvUtil {

    private static final Logger log = LogManager.getLogger(CsvUtil.class.getName());

    public static <T> void write(T object, String filePath, String[] columns) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        createFileIfNotExists(filePath);

        FileWriter fileWriter = new FileWriter(filePath, true);
        CSVWriter writer = new CSVWriter(fileWriter,
                Constants.CSV_DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
        mappingStrategy.setType(object.getClass());

        mappingStrategy.setColumnMapping(columns);

        StatefulBeanToCsvBuilder<Subject> builder = new StatefulBeanToCsvBuilder(writer);
        StatefulBeanToCsv beanWriter = builder.withMappingStrategy(mappingStrategy).build();

        beanWriter.write(object);

        writer.close();
    }

    public static Integer getNewObjectId(String filePath) throws IOException, CsvValidationException {
        log.debug("getNewSubjectId [1]");
        String[] line, last = null;
        File file = new File(filePath);
        if (file.exists()) {
            FileReader fileReader = new FileReader(filePath);
            CSVReader reader = new CSVReader(fileReader);

            while ((line = reader.readNext()) != null) {
                last = line;
            }

            reader.close();

            if (last != null && last.length > 0) {
                String[] strings = last[0].split(";");
                if (!strings[0].isEmpty()) {
                    return Integer.parseInt(strings[0]) + 1;
                }
            }
        }
        return 1;
    }
}

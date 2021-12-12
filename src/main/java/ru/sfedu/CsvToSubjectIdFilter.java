package ru.sfedu;

import com.opencsv.bean.CsvToBeanFilter;
import ru.sfedu.utils.Constants;

import java.util.Arrays;

public class CsvToSubjectIdFilter implements CsvToBeanFilter {

    private final String id;

    public CsvToSubjectIdFilter(String id){
        this.id = id;
    }

    @Override
    public boolean allowLine(String[] strings) {
        String[] subStrings = strings[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
        boolean blankLine = subStrings.length > 0 && subStrings[0].equals(id);
        System.out.println(Arrays.toString(strings) + " " + blankLine);
        return blankLine;
    }
}

package ru.sfedu.utils;

import ru.sfedu.model.Subject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubjectUtil {
    public static String[] getAllSubjectFields(Subject subject) {
        List<Field> mainClassFields = Arrays.stream(subject.getClass().getDeclaredFields().clone()).toList();
        List<Field> superClassFields = Arrays.stream(subject.getClass().getSuperclass().getDeclaredFields().clone()).toList();
        List<Field> allFields = new ArrayList<>();
        allFields.addAll(superClassFields);
        allFields.addAll(mainClassFields);

        String[] columns = new String[allFields.size()];
        for (int i = 0; i < allFields.size(); i++) {
            columns[i] = allFields.get(i).getName();
        }
        return columns;
    }

    public static <T> String[] getAllObjectFields(T object){
        List<Field> mainClassFields = Arrays.stream(object.getClass().getDeclaredFields().clone()).toList();
        String[] columns = new String[mainClassFields.size()];
        for (int i = 0; i < mainClassFields.size(); i++) {
            columns[i] = mainClassFields.get(i).getName();
        }
        return columns;
    }
}

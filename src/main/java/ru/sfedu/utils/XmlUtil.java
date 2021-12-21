package ru.sfedu.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.sfedu.model.OnlyId;
import ru.sfedu.model.Wrapper;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static ru.sfedu.utils.FileUtil.createFileIfNotExists;

public class XmlUtil {

    private static final Logger log = LogManager.getLogger(XmlUtil.class.getName());

    public static Wrapper readFile(String subjectsFilePath) throws Exception {
        File file = new File(subjectsFilePath);
        Serializer serializer = new Persister();
        Wrapper wrapper = serializer.read(Wrapper.class, file);
        return wrapper;
    }

    public static <T extends OnlyId> void write(String filePath, T object) throws Exception {
        try {
            createFileIfNotExists(filePath);
        } catch (Exception e) {
            log.error("write[1]: error = {}", e.getMessage());
        }

        Serializer serializer = new Persister();
        File file = new File(filePath);
        Wrapper<OnlyId> wrapper = new Wrapper<>();
        try {
            boolean isFound = false;
            wrapper = serializer.read(Wrapper.class, file);
            List<OnlyId> list = wrapper.getList();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(object.getId())) {
                    list.set(i, object);
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                list.add(object);
            }
        } catch (XMLStreamException e) {
            List list = new ArrayList<>();
            list.add(object);
            wrapper.setList(list);
        } catch (Exception e) {
            log.error("write[2]: error = {}", e.getMessage());
        }

        serializer.write(wrapper, file);
    }

    public static Integer getNewObjectId(String filepath) {
        log.info("getNewObjectId [1]: filePath = {}", filepath);
        File file = new File(filepath);
        List list = new ArrayList();
        Serializer serializer = new Persister();
        try {
            Wrapper subjectWrapper = serializer.read(Wrapper.class, file);
            list = subjectWrapper.getList();
            if (list.isEmpty()) {
                return 1;
            }
        } catch (FileNotFoundException | XMLStreamException e) {
            return 1;
        } catch (Exception e) {
            log.error("getNewObjectId [2]: error = {}", e.getMessage());
        }

        return ((OnlyId) list.get(list.size() - 1)).getId() + 1;
    }
}

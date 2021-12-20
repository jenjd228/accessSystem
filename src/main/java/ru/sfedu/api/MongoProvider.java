package ru.sfedu.api;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import ru.sfedu.Constants;
import ru.sfedu.model.CommandType;
import ru.sfedu.model.RepositoryType;

import java.util.Date;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static ru.sfedu.utils.ConfigurationUtil.getConfigurationEntry;

public class MongoProvider {
    //sudo systemctl start mongod.service

    private static final Logger logger = LogManager.getLogger(MongoProvider.class.getName());

    public static <T> void save(CommandType command, RepositoryType repositoryType,String bdName, T obj) {
        logger.info("save [1]: command = {}, type = {}, object = {}, dbName = {}", command, repositoryType, obj,bdName);
        try {
            MongoCollection<Document> collection = getCollection(obj.getClass(),bdName);

            Document document = new Document();
            document.put(Constants.MONGO_FIELD_TIME, new Date());
            document.put(Constants.MONGO_FIELD_COMMAND, command.toString());
            document.put(Constants.MONGO_FIELD_REPOSITORY, repositoryType.toString());
            document.put(Constants.MONGO_FIELD_OBJECT, obj);
            collection.insertOne(document);
        } catch (Exception e) {
            logger.error("save [2]: {}", e.getMessage());
        }
    }

    private static <T> MongoCollection<Document> getCollection(Class<T> clazz,String bdName) {
        MongoClient mongoClient = new MongoClient(
                getConfigurationEntry(Constants.MONGO_HOST),
                Integer.parseInt(getConfigurationEntry(Constants.MONGO_PORT)));

        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoDatabase database = mongoClient.getDatabase(bdName).withCodecRegistry(pojoCodecRegistry);
        return database.getCollection(clazz.getSimpleName().toLowerCase());
    }
}

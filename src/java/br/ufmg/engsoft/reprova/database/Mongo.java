package br.ufmg.engsoft.reprova.database;

import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Mongodb instance.
 */
public class Mongo {
	/**
	 * Logger instance.
	 */
	protected static final Logger logger = LoggerFactory.getLogger(Mongo.class);

	/**
	 * Full connection string, obtained from 'mongodb' environment variable.
	 */
	protected static final String endpoint = "mongodb://admin:123456@localhost:27017/database?authSource=admin&connectTimeoutMS=5000";
	//protected static final String endpoint = "mongodb+srv://reprova:reprova@cluster0.wrhdl.mongodb.net/reprova?retryWrites=true&w=majority";

	/**
	 * The mongodb driver instance.
	 */
	protected final MongoDatabase db;

	/**
	 * Instantiate for access in the given database.
	 * 
	 * @param db the database name.
	 */

	public Mongo(String db) {
		this.db = MongoClients.create(Mongo.endpoint).getDatabase(db);

		logger.info("connected to db '" + db + "'");
	}

	/**
	 * Gets the given collection in the database.
	 */
	public MongoCollection<Document> getCollection(String name) {
		return db.getCollection(name);
	}
}

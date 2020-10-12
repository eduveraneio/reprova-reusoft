package br.ufmg.engsoft.reprova.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

import org.bson.Document;
import org.bson.types.ObjectId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Classroom;

/**
 * DAO for Classroom class on mongodb.
 */
public class ClassroomsDAO {
  /**
   * Logger instance.
   */
  protected static final Logger logger = LoggerFactory.getLogger(ClassroomsDAO.class);

  /**
   * Json formatter.
   */
  protected final Json json;

  /**
   * Classroom collection.
   */
  protected final MongoCollection<Document> collection;

  /**
   * Basic constructor.
   * @param db    the database, mustn't be null
   * @param json  the json formatter for the database's documents, mustn't be null
   * @throws IllegalArgumentException  if any parameter is null
   */
  public ClassroomsDAO(Mongo db, Json json) {
    if (db == null)
      throw new IllegalArgumentException("db mustn't be null");

    if (json == null)
      throw new IllegalArgumentException("json mustn't be null");

    this.collection = db.getCollection("classrooms");

    this.json = json;
  }
  
  /**
   * Parse the given document.
   * @param document  the classroom document, mustn't be null
   * @throws IllegalArgumentException  if any parameter is null
   * @throws IllegalArgumentException  if the given document is an invalid classroom
   */
  protected Classroom parseDoc(Document document) {
    if (document == null)
      throw new IllegalArgumentException("document mustn't be null");

    var doc = document.toJson();

    logger.info("Fetched classroom: " + doc);

    try {
      var classroom = json
        .parse(doc, Classroom.Builder.class)
        .build();

      logger.info("Parsed classroom: " + classroom);

      return classroom;
    }
    catch (Exception e) {
      logger.error("Invalid document in database!", e);
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Get the classroom with the given id.
   * @param id  the classroom's id in the database.
   * @return The classroom, or null if no such classroom.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Classroom get(String id) {
    if (id == null)
      throw new IllegalArgumentException("id mustn't be null");

    var classroom = this.collection
    	     .find(eq(new ObjectId(id)))
    	     .map(this::parseDoc)
    	     .first();

    if (classroom == null)
      logger.info("No such classroom " + id);

    return classroom;
  }

  /**
   * List all the classrooms that match the given non-null parameters.
   * The classroom's statement is ommited.
   * @param name      the expected name, or null
   * @param email     the expected email, or null
   * @return The classrooms in the collection that match the given parameters, possibly
   *         empty.
   * @throws IllegalArgumentException  if there is an invalid classroom
   */
  public Collection<Classroom> list(String name) {
    var filters =
      Arrays.asList(
        name == null ? null : eq("name", name)
      )
      .stream()
      .filter(Objects::nonNull) // mongo won't allow null filters.
      .collect(Collectors.toList());

    var doc = filters.isEmpty() // mongo won't take null as a filter.
      ? this.collection.find()
      : this.collection.find(and(filters));

    var result = new ArrayList<Classroom>();

    doc.projection(fields(exclude("statement")))
      .map(this::parseDoc)
      .into(result);

    return result;
  }
  
  /**
   * Adds or updates the given classroom in the database.
   * If the given classroom has an id, update, otherwise add.
   * @param classroom  the classroom to be stored
   * @return Whether the classroom was successfully added.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public boolean add(Classroom classroom) {
    if (classroom == null)
      throw new IllegalArgumentException("classroom mustn't be null");

    Document doc = new Document()
      .append("name", classroom.name);

    var id = classroom.id;
    if (id != null) {
      var result = this.collection.replaceOne(
        eq(new ObjectId(id)),
        doc
      );

      if (!result.wasAcknowledged()) {
        logger.warn("Failed to replace classroom " + id);
        return false;
      }
    }
    else
      this.collection.insertOne(doc);

    logger.info("Stored classroom " + doc.get("_id"));

    return true;
  }


  /**
   * Remove the classroom with the given id from the collection.
   * @param id  the classroom id
   * @return Whether the given classroom was removed.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public boolean remove(String id) {
    if (id == null)
      throw new IllegalArgumentException("id mustn't be null");

    var result = this.collection.deleteOne(
      eq(new ObjectId(id))
    ).wasAcknowledged();

    if (result)
      logger.info("Deleted classroom " + id);
    else
      logger.warn("Failed to delete classroom " + id);

    return result;
  }
}

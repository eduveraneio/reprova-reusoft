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
import br.ufmg.engsoft.reprova.model.Person;

/**
 * DAO for Person class on mongodb.
 */
public class PersonsDAO {
  /**
   * Logger instance.
   */
  protected static final Logger logger = LoggerFactory.getLogger(PersonsDAO.class);

  /**
   * Json formatter.
   */
  protected final Json json;

  /**
   * Persons collection.
   */
  protected final MongoCollection<Document> collection;

  /**
   * Basic constructor.
   * @param db    the database, mustn't be null
   * @param json  the json formatter for the database's documents, mustn't be null
   * @throws IllegalArgumentException  if any parameter is null
   */
  public PersonsDAO(Mongo db, Json json) {
    if (db == null)
      throw new IllegalArgumentException("db mustn't be null");

    if (json == null)
      throw new IllegalArgumentException("json mustn't be null");

    this.collection = db.getCollection("persons");

    this.json = json;
  }
  
  /**
   * Parse the given document.
   * @param document  the person document, mustn't be null
   * @throws IllegalArgumentException  if any parameter is null
   * @throws IllegalArgumentException  if the given document is an invalid Person
   */
  protected Person parseDoc(Document document) {
    if (document == null)
      throw new IllegalArgumentException("document mustn't be null");

    var doc = document.toJson();

    logger.info("Fetched person: " + doc);

    try {
      var person = json
        .parse(doc, Person.Builder.class)
        .build();

      logger.info("Parsed person: " + person);

      return person;
    }
    catch (Exception e) {
      logger.error("Invalid document in database!", e);
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Get the person with the given id.
   * @param id  the person's id in the database.
   * @return The person, or null if no such person.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Person get(String id) {
    if (id == null)
      throw new IllegalArgumentException("id mustn't be null");

    var person = this.collection
    	     .find(eq(new ObjectId(id)))
    	     .map(this::parseDoc)
    	     .first();

    if (person == null)
      logger.info("No such person " + id);

    return person;
  }

  /**
   * List all the persons that match the given non-null parameters.
   * The person's statement is ommited.
   * @param name      the expected name, or null
   * @param email     the expected email, or null
   * @return The persons in the collection that match the given parameters, possibly
   *         empty.
   * @throws IllegalArgumentException  if there is an invalid Person
   */
  public Collection<Person> list(String name, String email, String type) {
    var filters =
      Arrays.asList(
        name == null ? null : eq("name", name),
        email == null ? null : eq("email", email),
        type == null ? null : eq("type", type)
      )
      .stream()
      .filter(Objects::nonNull) // mongo won't allow null filters.
      .collect(Collectors.toList());

    var doc = filters.isEmpty() // mongo won't take null as a filter.
      ? this.collection.find()
      : this.collection.find(and(filters));

    var result = new ArrayList<Person>();

    doc.projection(fields(exclude("statement")))
      .map(this::parseDoc)
      .into(result);

    return result;
  }
  
  /**
   * Adds or updates the given person in the database.
   * If the given person has an id, update, otherwise add.
   * @param person  the person to be stored
   * @return Whether the person was successfully added.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public boolean add(Person person) {
    if (person == null)
      throw new IllegalArgumentException("person mustn't be null");

    Document doc = new Document()
      .append("name", person.name)
      .append("email", person.email)
      .append("password", person.password);

    var id = person.id;
    if (id != null) {
      var result = this.collection.replaceOne(
        eq(new ObjectId(id)),
        doc
      );

      if (!result.wasAcknowledged()) {
        logger.warn("Failed to replace person " + id);
        return false;
      }
    }
    else
      this.collection.insertOne(doc);

    logger.info("Stored person " + doc.get("_id"));

    return true;
  }


  /**
   * Remove the person with the given id from the collection.
   * @param id  the person id
   * @return Whether the given person was removed.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public boolean remove(String id) {
    if (id == null)
      throw new IllegalArgumentException("id mustn't be null");

    var result = this.collection.deleteOne(
      eq(new ObjectId(id))
    ).wasAcknowledged();

    if (result)
      logger.info("Deleted person " + id);
    else
      logger.warn("Failed to delete person " + id);

    return result;
  }
}

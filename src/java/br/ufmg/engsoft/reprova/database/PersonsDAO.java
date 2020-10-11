package br.ufmg.engsoft.reprova.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
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
   * Questions collection.
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
   * @param id  the question's id in the database.
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
   * List all the questions that match the given non-null parameters.
   * The question's statement is ommited.
   * @param theme      the expected theme, or null
   * @param pvt        the expected privacy, or null
   * @return The questions in the collection that match the given parameters, possibly
   *         empty.
   * @throws IllegalArgumentException  if there is an invalid Question
   */
  public Collection<Person> list(String name, String email) {
    var filters =
      Arrays.asList(
        name == null ? null : eq("name", name),
        email == null ? null : eq("email", email)
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
}
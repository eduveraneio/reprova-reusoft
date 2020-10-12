package br.ufmg.engsoft.reprova.routes.api;

import spark.Spark;
import spark.Request;
import spark.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.database.PersonsDAO;
import br.ufmg.engsoft.reprova.model.Person;
import br.ufmg.engsoft.reprova.mime.json.JsonPerson;

/**
 * Persons route.
 */
public class Persons {
  /**
   * Logger instance.
   */
  protected static final Logger logger = LoggerFactory.getLogger(Persons.class);

  /**
   * Access token.
   */
  protected static final String token = "d2fad245dd1d8a4f863e3f1c32bdada723361e6f63cfddf56663e516e47347bb";

  /**
   * Messages.
   */
  protected static final String unauthorised = "\"Unauthorised\"";
  protected static final String invalid = "\"Invalid request\"";
  protected static final String ok = "\"Ok\"";

  /**
   * Json formatter.
   */
  protected final JsonPerson json;
  /**
   * DAO for Person.
   */
  protected final PersonsDAO personsDAO;

  /**
   * Instantiate the persons endpoint.
   * The setup method must be called to install the endpoint.
   * @param json          the json formatter
   * @param personsDAO  the DAO for Person
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Persons(JsonPerson json, PersonsDAO personsDAO) {
    if (json == null)
      throw new IllegalArgumentException("json mustn't be null");

    if (personsDAO == null)
      throw new IllegalArgumentException("personsDAO mustn't be null");

    this.json = json;
    this.personsDAO = personsDAO;
  }

  /**
   * Install the endpoint in Spark.
   * Methods:
   * - get
   * - post
   * - delete
   */
  public void setup() {
    Spark.get("/api/persons", this::get);
    Spark.post("/api/persons", this::post);
    Spark.delete("/api/persons", this::delete);

    logger.info("Setup /api/persons.");
  }


  /**
   * Check if the given token is authorised.
   */
  protected static boolean authorised(String token) {
    //return Persons.token.equals(token);
	 return true;
  }


  /**
   * Get endpoint: lists all persons, or a single person if a 'id' query parameter is
   * provided.
   */
  protected Object get(Request request, Response response) {
    logger.info("Received persons get:");

    var id = request.queryParams("id");
    var type = request.queryParams("type");
    var auth = authorised(request.queryParams("token"));

    return id != null
      ? this.getById(request, response, id, auth)
      : this.getByType(request, response, type, auth);
  }

  /**
   * Get id endpoint: fetch the specified person from the database.
   * If not authorised, and the given person is private, returns an error message.
   */
  protected Object getById(Request request, Response response, String id, boolean auth) {
    if (id == null)
      throw new IllegalArgumentException("id mustn't be null");

    response.type("application/json");

    logger.info("Fetching question " + id);

    var person = personsDAO.get(id);

    if (person == null) {
      logger.error("Invalid request!");
      response.status(400);
      return invalid;
    }

    if (!auth) {
      logger.info("Unauthorised token: " + token);
      response.status(403);
      return unauthorised;
    }

    logger.info("Done. Responding...");

    response.status(200);

    return json.render(person);
  }

  /**
   * Get all endpoint: fetch all persons from the database.
   * If not authorised, fetches only public persons.
   */
  protected Object getByType(Request request, Response response, String type, boolean auth) {
    response.type("application/json");

    logger.info("Fetching persons.");
    
    var persons = personsDAO.list(null, null, type);

    logger.info("Done. Responding...");

    response.status(200);

    return json.render(persons);
  }

  /**
   * Post endpoint: add or update a person in the database.
   * The person must be supplied in the request's body.
   * If the person has an 'id' field, the operation is an update.
   * Otherwise, the given person is added as a new person in the database.
   * This endpoint is for authorized access only.
   */
  protected Object post(Request request, Response response) {
    String body = request.body();

    logger.info("Received persons post:" + body);

    response.type("application/json");

    var token = request.queryParams("token");

    if (!authorised(token)) {
      logger.info("Unauthorised token: " + token);
      response.status(403);
      return unauthorised;
    }

    Person person;
    try {
       person = json
        .parse(body, Person.Builder.class)
        .build();
    }
    catch (Exception e) {
      logger.error("Invalid request payload!", e);
      response.status(400);
      return invalid;
    }

    logger.info("Parsed " + person.toString());

    logger.info("Adding person.");

    var success = personsDAO.add(person);

    response.status(
       success ? 200
               : 400
    );

    logger.info("Done. Responding...");

    return ok;
  }

  /**
   * Delete endpoint: remove a question from the database.
   * The persons's id must be supplied through the 'id' query parameter.
   * This endpoint is for authorized access only.
   */
  protected Object delete(Request request, Response response) {
    logger.info("Received persons delete:");

    response.type("application/json");

    var id = request.queryParams("id");
    var token = request.queryParams("token");

    if (!authorised(token)) {
      logger.info("Unauthorised token: " + token);
      response.status(403);
      return unauthorised;
    }

    if (id == null) {
      logger.error("Invalid request!");
      response.status(400);
      return invalid;
    }

    logger.info("Deleting person " + id);

    var success = personsDAO.remove(id);

    logger.info("Done. Responding...");

    response.status(
      success ? 200
              : 400
    );

    return ok;
  }
}

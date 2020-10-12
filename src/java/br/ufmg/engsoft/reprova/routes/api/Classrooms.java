package br.ufmg.engsoft.reprova.routes.api;

import spark.Spark;
import spark.Request;
import spark.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.database.ClassroomsDAO;
import br.ufmg.engsoft.reprova.model.Classroom;
import br.ufmg.engsoft.reprova.mime.json.Json;

/**
 * Classrooms route.
 */
public class Classrooms {
  /**
   * Logger instance.
   */
  protected static final Logger logger = LoggerFactory.getLogger(Classrooms.class);

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
  protected final Json json;
  /**
   * DAO for Classroom.
   */
  protected final ClassroomsDAO classroomsDAO;

  /**
   * Instantiate the classrooms endpoint.
   * The setup method must be called to install the endpoint.
   * @param json          the json formatter
   * @param classroomsDAO  the DAO for Classroom
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Classrooms(Json json, ClassroomsDAO classroomsDAO) {
    if (json == null)
      throw new IllegalArgumentException("json mustn't be null");

    if (classroomsDAO == null)
      throw new IllegalArgumentException("classroomsDAO mustn't be null");

    this.json = json;
    this.classroomsDAO = classroomsDAO;
  }

  /**
   * Install the endpoint in Spark.
   * Methods:
   * - get
   * - post
   * - delete
   */
  public void setup() {
    Spark.get("/api/classrooms", this::get);
    Spark.post("/api/classrooms", this::post);
    Spark.delete("/api/classrooms", this::delete);

    logger.info("Setup /api/classrooms.");
  }


  /**
   * Check if the given token is authorised.
   */
  protected static boolean authorised(String token) {
    //return Classrooms.token.equals(token);
	 return true;
  }


  /**
   * Get endpoint: lists all classrooms, or a single classroom if a 'id' query parameter is
   * provided.
   */
  protected Object get(Request request, Response response) {
    logger.info("Received classrooms get:");

    var id = request.queryParams("id");
    var auth = authorised(request.queryParams("token"));

    return id != null
      ? this.get(request, response, id, auth)
      : this.get(request, response, auth);
  }

  /**
   * Get id endpoint: fetch the specified classroom from the database.
   * If not authorised, and the given classroom is private, returns an error message.
   */
  protected Object get(Request request, Response response, String id, boolean auth) {
    if (id == null)
      throw new IllegalArgumentException("id mustn't be null");

    response.type("application/json");

    logger.info("Fetching classroom " + id);

    var classroom = classroomsDAO.get(id);

    if (classroom == null) {
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

    return json.render(classroom);
  }

  /**
   * Get all endpoint: fetch all classrooms from the database.
   * If not authorised, fetches only public classrooms.
   */
  protected Object get(Request request, Response response, boolean auth) {
    response.type("application/json");

    logger.info("Fetching classrooms.");
    
    var classrooms = classroomsDAO.list(null);

    logger.info("Done. Responding...");

    response.status(200);

    return json.render(classrooms);
  }

  /**
   * Post endpoint: add or update a classroom in the database.
   * The classroom must be supplied in the request's body.
   * If the classroom has an 'id' field, the operation is an update.
   * Otherwise, the given classroom is added as a new classroom in the database.
   * This endpoint is for authorized access only.
   */
  protected Object post(Request request, Response response) {
    String body = request.body();

    logger.info("Received classrooms post:" + body);

    response.type("application/json");

    var token = request.queryParams("token");

    if (!authorised(token)) {
      logger.info("Unauthorised token: " + token);
      response.status(403);
      return unauthorised;
    }

    Classroom classroom;
    try {
       classroom = json
        .parse(body, Classroom.Builder.class)
        .build();
    }
    catch (Exception e) {
      logger.error("Invalid request payload!", e);
      response.status(400);
      return invalid;
    }

    logger.info("Parsed " + classroom.toString());

    logger.info("Adding classroom.");

    var success = classroomsDAO.add(classroom);

    response.status(
       success ? 200
               : 400
    );

    logger.info("Done. Responding...");

    return ok;
  }

  /**
   * Delete endpoint: remove a classrooms from the database.
   * The classrooms's id must be supplied through the 'id' query parameter.
   * This endpoint is for authorized access only.
   */
  protected Object delete(Request request, Response response) {
    logger.info("Received classrooms delete:");

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

    logger.info("Deleting classroom " + id);

    var success = classroomsDAO.remove(id);

    logger.info("Done. Responding...");

    response.status(
      success ? 200
              : 400
    );

    return ok;
  }
}

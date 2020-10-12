package br.ufmg.engsoft.reprova.routes;

import spark.Spark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.database.Mongo;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.database.PersonsDAO;
import br.ufmg.engsoft.reprova.database.ClassroomsDAO;
import br.ufmg.engsoft.reprova.routes.api.Questions;
import br.ufmg.engsoft.reprova.routes.api.Persons;
import br.ufmg.engsoft.reprova.routes.api.Classrooms;

/**
 * Service setup class.
 * This class is static.
 */
public class Setup {
  /**
   * Static class.
   */
  protected Setup() {  }

  /**
   * Logger instance.
   */
  protected static Logger logger = LoggerFactory.getLogger(Setup.class);

  /**
   * The port for the webserver.
   */
  protected static final int port = 8080;


  /**
   * Setup the service routes.
   * This sets up the routes under the routes directory,
   * and also static files on '/public'.
   * @param json          the json formatter
   * @param personDAO  the DAO for Person
   * @throws IllegalArgumentException  if any parameter is null
   */
  public static void routes(Mongo db) {
    Spark.port(Setup.port);

    logger.info("Spark on port " + Setup.port);
    
    logger.info("Setting up static resources.");
    Spark.staticFiles.location("/public");
    
    questionRoute(db);
    personRoute(db);
    classroomRoute(db);
  }

  private static void questionRoute(Mongo db) {
	logger.info("Setting up questions route:");
    var questionsJson = new Json();
    var questionsDAO  = new QuestionsDAO(db, questionsJson);
    var questions = new Questions(questionsJson, questionsDAO);
    questions.setup();
  }
  
  private static void personRoute(Mongo db) {
	logger.info("Setting up persons route:");
    var personsJson = new Json();
    var personsDAO  = new PersonsDAO(db, personsJson);
    var persons = new Persons(personsJson, personsDAO);
    persons.setup();
  }
  
  private static void classroomRoute(Mongo db) {
	logger.info("Setting up classrooms route:");
    var classroomsJson = new Json();
    var classroomsDAO  = new ClassroomsDAO(db, classroomsJson);
    var classrooms = new Classrooms(classroomsJson, classroomsDAO);
    classrooms.setup();
  }
}

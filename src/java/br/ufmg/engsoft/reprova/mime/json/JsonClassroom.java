package br.ufmg.engsoft.reprova.mime.json;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import br.ufmg.engsoft.reprova.model.Classroom;

/**
 * Json format for Reprova's types.
 */
public class JsonClassroom {

	/**
   * Deserializer for Classroom.Builder.
   */
  protected static class ClassroomBuilderDeserializer
    implements JsonDeserializer<Classroom.Builder>
  {
    @Override
    public Classroom.Builder deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context
    ) {
      var parserBuilder = new GsonBuilder();

      var classroomBuilder = parserBuilder
        .create()
        .fromJson(
          json.getAsJsonObject(),
          Classroom.Builder.class
        );

      // Mongo's id property doesn't match Person.id:
      var _id = json.getAsJsonObject().get("_id");

      if (_id != null)
    	  classroomBuilder.id(
          _id.getAsJsonObject()
            .get("$oid")
            .getAsString()
        );

      return classroomBuilder;
    }
  }



  /**
   * The json formatter.
   */
  protected Gson gson;



  /**
   * Instantiate the formatter for Reprova's types.
   * Currently, it supports only the Question type.
   */
  public JsonClassroom() {
    var parserBuilder = new GsonBuilder();

    parserBuilder.registerTypeAdapter(
      Classroom.Builder.class,
      new ClassroomBuilderDeserializer()
    );

    this.gson = parserBuilder.create();
  }



  /**
   * Parse an object in the given class.
   * @throws JsonSyntaxException  if json is not a valid representation for the given class
   */
  public <T> T parse(String json, Class<T> cls) {
    return this.gson.fromJson(json, cls);
  }


  /**
   * Render an object of the given class.
   */
  public <T> String render(T obj) {
    return this.gson.toJson(obj);
  }
}

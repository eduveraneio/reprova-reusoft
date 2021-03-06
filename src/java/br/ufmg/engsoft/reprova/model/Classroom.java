package br.ufmg.engsoft.reprova.model;

import java.util.Objects;

/**
 * The classroom type.
 */
public class Classroom {
  /**
   * The id of the classroom.
   * When null, the id will be automatically generated by the database.
   */
  public final String id;
  /**
   * The name of the classroom. Mustn't be null nor empty.
   */
  public final String name;
  

  /**
   * Builder for classroom.
   */
  public static class Builder {
    protected String id;
    protected String name;
   
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Build the classroom.
     * @throws IllegalArgumentException  if any parameter is invalid
     */
    public Classroom build() {
      if (name == null)
        throw new IllegalArgumentException("name mustn't be null");

      if (name.isEmpty())
        throw new IllegalArgumentException("name mustn't be empty");
      
      return new Classroom(
        this.id,
        this.name
      );
    }
  }

  /**
   * Protected constructor, should only be used by the builder.
   */
  protected Classroom(
    String id,
    String name
  ) {
    this.id = id;
    this.name = name;
  }

  /**
   * Equality comparison.
   * Although this object has an id, equality is checked on all fields.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;

    if (!(obj instanceof Classroom))
      return false;

    var classroom = (Classroom) obj;

    return this.id.equals(classroom.id)
        && this.name.equals(classroom.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      this.id,
      this.name
    );
  }

  /**
   * Convert a classroom to String for visualization purposes.
   */
  @Override
  public String toString() {
    var builder = new StringBuilder();

    builder.append("Classroom:\n");
    builder.append("  id: " + this.id + "\n");
    builder.append("  name: " + this.name + "\n");

    return builder.toString();
  }
}


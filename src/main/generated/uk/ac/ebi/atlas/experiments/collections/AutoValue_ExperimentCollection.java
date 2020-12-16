

package uk.ac.ebi.atlas.experiments.collections;

import java.awt.Image;
import java.util.Optional;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_ExperimentCollection extends ExperimentCollection {

  private final String id;

  private final String name;

  private final String description;

  private final Optional<Image> icon;

  AutoValue_ExperimentCollection(
      String id,
      String name,
      String description,
      Optional<Image> icon) {
    if (id == null) {
      throw new NullPointerException("Null id");
    }
    this.id = id;
    if (name == null) {
      throw new NullPointerException("Null name");
    }
    this.name = name;
    if (description == null) {
      throw new NullPointerException("Null description");
    }
    this.description = description;
    if (icon == null) {
      throw new NullPointerException("Null icon");
    }
    this.icon = icon;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String description() {
    return description;
  }

  @Override
  public Optional<Image> icon() {
    return icon;
  }

  @Override
  public String toString() {
    return "ExperimentCollection{"
         + "id=" + id + ", "
         + "name=" + name + ", "
         + "description=" + description + ", "
         + "icon=" + icon
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentCollection) {
      ExperimentCollection that = (ExperimentCollection) o;
      return (this.id.equals(that.id()))
           && (this.name.equals(that.name()))
           && (this.description.equals(that.description()))
           && (this.icon.equals(that.icon()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= id.hashCode();
    h$ *= 1000003;
    h$ ^= name.hashCode();
    h$ *= 1000003;
    h$ ^= description.hashCode();
    h$ *= 1000003;
    h$ ^= icon.hashCode();
    return h$;
  }

}



package uk.ac.ebi.atlas.model.download;

import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_ExternallyAvailableContent_Description extends ExternallyAvailableContent.Description {

  private final String group;

  private final String type;

  private final String description;

  AutoValue_ExternallyAvailableContent_Description(
      String group,
      String type,
      String description) {
    if (group == null) {
      throw new NullPointerException("Null group");
    }
    this.group = group;
    if (type == null) {
      throw new NullPointerException("Null type");
    }
    this.type = type;
    if (description == null) {
      throw new NullPointerException("Null description");
    }
    this.description = description;
  }

  @Override
  public String group() {
    return group;
  }

  @Override
  public String type() {
    return type;
  }

  @Override
  public String description() {
    return description;
  }

  @Override
  public String toString() {
    return "Description{"
         + "group=" + group + ", "
         + "type=" + type + ", "
         + "description=" + description
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExternallyAvailableContent.Description) {
      ExternallyAvailableContent.Description that = (ExternallyAvailableContent.Description) o;
      return (this.group.equals(that.group()))
           && (this.type.equals(that.type()))
           && (this.description.equals(that.description()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= group.hashCode();
    h$ *= 1000003;
    h$ ^= type.hashCode();
    h$ *= 1000003;
    h$ ^= description.hashCode();
    return h$;
  }

}



package uk.ac.ebi.atlas.search;

import java.util.Optional;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_SemanticQueryTerm extends SemanticQueryTerm {

  private final String value;

  private final Optional<String> category;

  AutoValue_SemanticQueryTerm(
      String value,
      Optional<String> category) {
    if (value == null) {
      throw new NullPointerException("Null value");
    }
    this.value = value;
    if (category == null) {
      throw new NullPointerException("Null category");
    }
    this.category = category;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public Optional<String> category() {
    return category;
  }

  @Override
  public String toString() {
    return "SemanticQueryTerm{"
         + "value=" + value + ", "
         + "category=" + category
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SemanticQueryTerm) {
      SemanticQueryTerm that = (SemanticQueryTerm) o;
      return (this.value.equals(that.value()))
           && (this.category.equals(that.category()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= value.hashCode();
    h$ *= 1000003;
    h$ ^= category.hashCode();
    return h$;
  }

}

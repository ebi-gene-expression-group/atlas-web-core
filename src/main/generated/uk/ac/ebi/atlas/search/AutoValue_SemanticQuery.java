

package uk.ac.ebi.atlas.search;

import com.google.common.collect.ImmutableSet;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_SemanticQuery extends SemanticQuery {

  private final ImmutableSet<SemanticQueryTerm> terms;

  AutoValue_SemanticQuery(
      ImmutableSet<SemanticQueryTerm> terms) {
    if (terms == null) {
      throw new NullPointerException("Null terms");
    }
    this.terms = terms;
  }

  @Override
  public ImmutableSet<SemanticQueryTerm> terms() {
    return terms;
  }

  @Override
  public String toString() {
    return "SemanticQuery{"
         + "terms=" + terms
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SemanticQuery) {
      SemanticQuery that = (SemanticQuery) o;
      return (this.terms.equals(that.terms()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= terms.hashCode();
    return h$;
  }

}

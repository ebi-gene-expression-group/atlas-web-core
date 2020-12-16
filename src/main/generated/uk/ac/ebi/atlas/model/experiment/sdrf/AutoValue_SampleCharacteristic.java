

package uk.ac.ebi.atlas.model.experiment.sdrf;

import java.util.Set;
import javax.annotation.processing.Generated;
import uk.ac.ebi.atlas.model.OntologyTerm;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_SampleCharacteristic extends SampleCharacteristic {

  private final String header;

  private final String value;

  private final Set<OntologyTerm> valueOntologyTerms;

  AutoValue_SampleCharacteristic(
      String header,
      String value,
      Set<OntologyTerm> valueOntologyTerms) {
    if (header == null) {
      throw new NullPointerException("Null header");
    }
    this.header = header;
    if (value == null) {
      throw new NullPointerException("Null value");
    }
    this.value = value;
    if (valueOntologyTerms == null) {
      throw new NullPointerException("Null valueOntologyTerms");
    }
    this.valueOntologyTerms = valueOntologyTerms;
  }

  @Override
  public String getHeader() {
    return header;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public Set<OntologyTerm> getValueOntologyTerms() {
    return valueOntologyTerms;
  }

  @Override
  public String toString() {
    return "SampleCharacteristic{"
         + "header=" + header + ", "
         + "value=" + value + ", "
         + "valueOntologyTerms=" + valueOntologyTerms
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SampleCharacteristic) {
      SampleCharacteristic that = (SampleCharacteristic) o;
      return (this.header.equals(that.getHeader()))
           && (this.value.equals(that.getValue()))
           && (this.valueOntologyTerms.equals(that.getValueOntologyTerms()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= header.hashCode();
    h$ *= 1000003;
    h$ ^= value.hashCode();
    h$ *= 1000003;
    h$ ^= valueOntologyTerms.hashCode();
    return h$;
  }

}

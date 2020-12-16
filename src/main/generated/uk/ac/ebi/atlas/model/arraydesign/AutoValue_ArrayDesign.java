

package uk.ac.ebi.atlas.model.arraydesign;

import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_ArrayDesign extends ArrayDesign {

  private final String accession;

  private final String name;

  AutoValue_ArrayDesign(
      String accession,
      String name) {
    if (accession == null) {
      throw new NullPointerException("Null accession");
    }
    this.accession = accession;
    if (name == null) {
      throw new NullPointerException("Null name");
    }
    this.name = name;
  }

  @Override
  public String getAccession() {
    return accession;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "ArrayDesign{"
         + "accession=" + accession + ", "
         + "name=" + name
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ArrayDesign) {
      ArrayDesign that = (ArrayDesign) o;
      return (this.accession.equals(that.getAccession()))
           && (this.name.equals(that.getName()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= accession.hashCode();
    h$ *= 1000003;
    h$ ^= name.hashCode();
    return h$;
  }

}

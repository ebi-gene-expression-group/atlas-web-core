

package uk.ac.ebi.atlas.model;

import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_OntologyTerm extends OntologyTerm {

  private final String accession;

  private final String name;

  private final String source;

  private final int depth;

  AutoValue_OntologyTerm(
      String accession,
      String name,
      String source,
      int depth) {
    if (accession == null) {
      throw new NullPointerException("Null accession");
    }
    this.accession = accession;
    if (name == null) {
      throw new NullPointerException("Null name");
    }
    this.name = name;
    if (source == null) {
      throw new NullPointerException("Null source");
    }
    this.source = source;
    this.depth = depth;
  }

  @Override
  public String accession() {
    return accession;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String source() {
    return source;
  }

  @Override
  public int depth() {
    return depth;
  }

  @Override
  public String toString() {
    return "OntologyTerm{"
         + "accession=" + accession + ", "
         + "name=" + name + ", "
         + "source=" + source + ", "
         + "depth=" + depth
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof OntologyTerm) {
      OntologyTerm that = (OntologyTerm) o;
      return (this.accession.equals(that.accession()))
           && (this.name.equals(that.name()))
           && (this.source.equals(that.source()))
           && (this.depth == that.depth());
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
    h$ *= 1000003;
    h$ ^= source.hashCode();
    h$ *= 1000003;
    h$ ^= depth;
    return h$;
  }

}

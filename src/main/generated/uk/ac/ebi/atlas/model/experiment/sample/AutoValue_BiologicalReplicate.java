

package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.common.collect.ImmutableSet;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_BiologicalReplicate extends BiologicalReplicate {

  private final String id;

  private final ImmutableSet<String> assayIds;

  AutoValue_BiologicalReplicate(
      String id,
      ImmutableSet<String> assayIds) {
    if (id == null) {
      throw new NullPointerException("Null id");
    }
    this.id = id;
    if (assayIds == null) {
      throw new NullPointerException("Null assayIds");
    }
    this.assayIds = assayIds;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public ImmutableSet<String> getAssayIds() {
    return assayIds;
  }

  @Override
  public String toString() {
    return "BiologicalReplicate{"
         + "id=" + id + ", "
         + "assayIds=" + assayIds
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof BiologicalReplicate) {
      BiologicalReplicate that = (BiologicalReplicate) o;
      return (this.id.equals(that.getId()))
           && (this.assayIds.equals(that.getAssayIds()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= id.hashCode();
    h$ *= 1000003;
    h$ ^= assayIds.hashCode();
    return h$;
  }

}

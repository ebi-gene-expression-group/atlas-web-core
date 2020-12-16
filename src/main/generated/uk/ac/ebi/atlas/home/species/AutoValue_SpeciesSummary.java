

package uk.ac.ebi.atlas.home.species;

import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_SpeciesSummary extends SpeciesSummary {

  private final String species;

  private final String kingdom;

  private final long totalExperiments;

  private final long baselineExperiments;

  private final long differentialExperiments;

  AutoValue_SpeciesSummary(
      String species,
      String kingdom,
      long totalExperiments,
      long baselineExperiments,
      long differentialExperiments) {
    if (species == null) {
      throw new NullPointerException("Null species");
    }
    this.species = species;
    if (kingdom == null) {
      throw new NullPointerException("Null kingdom");
    }
    this.kingdom = kingdom;
    this.totalExperiments = totalExperiments;
    this.baselineExperiments = baselineExperiments;
    this.differentialExperiments = differentialExperiments;
  }

  @Override
  public String getSpecies() {
    return species;
  }

  @Override
  public String getKingdom() {
    return kingdom;
  }

  @Override
  public long getTotalExperiments() {
    return totalExperiments;
  }

  @Override
  public long getBaselineExperiments() {
    return baselineExperiments;
  }

  @Override
  public long getDifferentialExperiments() {
    return differentialExperiments;
  }

  @Override
  public String toString() {
    return "SpeciesSummary{"
         + "species=" + species + ", "
         + "kingdom=" + kingdom + ", "
         + "totalExperiments=" + totalExperiments + ", "
         + "baselineExperiments=" + baselineExperiments + ", "
         + "differentialExperiments=" + differentialExperiments
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SpeciesSummary) {
      SpeciesSummary that = (SpeciesSummary) o;
      return (this.species.equals(that.getSpecies()))
           && (this.kingdom.equals(that.getKingdom()))
           && (this.totalExperiments == that.getTotalExperiments())
           && (this.baselineExperiments == that.getBaselineExperiments())
           && (this.differentialExperiments == that.getDifferentialExperiments());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= species.hashCode();
    h$ *= 1000003;
    h$ ^= kingdom.hashCode();
    h$ *= 1000003;
    h$ ^= (int) ((totalExperiments >>> 32) ^ totalExperiments);
    h$ *= 1000003;
    h$ ^= (int) ((baselineExperiments >>> 32) ^ baselineExperiments);
    h$ *= 1000003;
    h$ ^= (int) ((differentialExperiments >>> 32) ^ differentialExperiments);
    return h$;
  }

}

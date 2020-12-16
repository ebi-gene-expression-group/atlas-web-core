

package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_SpeciesProperties extends SpeciesProperties {

  private final String ensemblName;

  private final String defaultQueryFactorType;

  private final String kingdom;

  private final ImmutableCollection<ImmutableMap<String, String>> resources;

  AutoValue_SpeciesProperties(
      String ensemblName,
      String defaultQueryFactorType,
      String kingdom,
      ImmutableCollection<ImmutableMap<String, String>> resources) {
    if (ensemblName == null) {
      throw new NullPointerException("Null ensemblName");
    }
    this.ensemblName = ensemblName;
    if (defaultQueryFactorType == null) {
      throw new NullPointerException("Null defaultQueryFactorType");
    }
    this.defaultQueryFactorType = defaultQueryFactorType;
    if (kingdom == null) {
      throw new NullPointerException("Null kingdom");
    }
    this.kingdom = kingdom;
    if (resources == null) {
      throw new NullPointerException("Null resources");
    }
    this.resources = resources;
  }

  @Override
  public String ensemblName() {
    return ensemblName;
  }

  @Override
  public String defaultQueryFactorType() {
    return defaultQueryFactorType;
  }

  @Override
  public String kingdom() {
    return kingdom;
  }

  @Override
  public ImmutableCollection<ImmutableMap<String, String>> resources() {
    return resources;
  }

  @Override
  public String toString() {
    return "SpeciesProperties{"
         + "ensemblName=" + ensemblName + ", "
         + "defaultQueryFactorType=" + defaultQueryFactorType + ", "
         + "kingdom=" + kingdom + ", "
         + "resources=" + resources
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SpeciesProperties) {
      SpeciesProperties that = (SpeciesProperties) o;
      return (this.ensemblName.equals(that.ensemblName()))
           && (this.defaultQueryFactorType.equals(that.defaultQueryFactorType()))
           && (this.kingdom.equals(that.kingdom()))
           && (this.resources.equals(that.resources()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= ensemblName.hashCode();
    h$ *= 1000003;
    h$ ^= defaultQueryFactorType.hashCode();
    h$ *= 1000003;
    h$ ^= kingdom.hashCode();
    h$ *= 1000003;
    h$ ^= resources.hashCode();
    return h$;
  }

}

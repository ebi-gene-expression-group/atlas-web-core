

package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_ExperimentDisplayDefaults extends ExperimentDisplayDefaults {

  private final String defaultQueryFactorType;

  private final ImmutableMap<String, String> defaultFilterValues;

  private final ImmutableSet<String> factorTypes;

  private final boolean columnOrderPreserved;

  AutoValue_ExperimentDisplayDefaults(
      String defaultQueryFactorType,
      ImmutableMap<String, String> defaultFilterValues,
      ImmutableSet<String> factorTypes,
      boolean columnOrderPreserved) {
    if (defaultQueryFactorType == null) {
      throw new NullPointerException("Null defaultQueryFactorType");
    }
    this.defaultQueryFactorType = defaultQueryFactorType;
    if (defaultFilterValues == null) {
      throw new NullPointerException("Null defaultFilterValues");
    }
    this.defaultFilterValues = defaultFilterValues;
    if (factorTypes == null) {
      throw new NullPointerException("Null factorTypes");
    }
    this.factorTypes = factorTypes;
    this.columnOrderPreserved = columnOrderPreserved;
  }

  @Override
  public String getDefaultQueryFactorType() {
    return defaultQueryFactorType;
  }

  @Override
  public ImmutableMap<String, String> getDefaultFilterValues() {
    return defaultFilterValues;
  }

  @Override
  public ImmutableSet<String> getFactorTypes() {
    return factorTypes;
  }

  @Override
  public boolean isColumnOrderPreserved() {
    return columnOrderPreserved;
  }

  @Override
  public String toString() {
    return "ExperimentDisplayDefaults{"
         + "defaultQueryFactorType=" + defaultQueryFactorType + ", "
         + "defaultFilterValues=" + defaultFilterValues + ", "
         + "factorTypes=" + factorTypes + ", "
         + "columnOrderPreserved=" + columnOrderPreserved
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentDisplayDefaults) {
      ExperimentDisplayDefaults that = (ExperimentDisplayDefaults) o;
      return (this.defaultQueryFactorType.equals(that.getDefaultQueryFactorType()))
           && (this.defaultFilterValues.equals(that.getDefaultFilterValues()))
           && (this.factorTypes.equals(that.getFactorTypes()))
           && (this.columnOrderPreserved == that.isColumnOrderPreserved());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= defaultQueryFactorType.hashCode();
    h$ *= 1000003;
    h$ ^= defaultFilterValues.hashCode();
    h$ *= 1000003;
    h$ ^= factorTypes.hashCode();
    h$ *= 1000003;
    h$ ^= columnOrderPreserved ? 1231 : 1237;
    return h$;
  }

}

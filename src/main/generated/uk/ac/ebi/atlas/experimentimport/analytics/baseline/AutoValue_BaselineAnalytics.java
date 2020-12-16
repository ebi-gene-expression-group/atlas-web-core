

package uk.ac.ebi.atlas.experimentimport.analytics.baseline;

import java.util.Arrays;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_BaselineAnalytics extends BaselineAnalytics {

  private final String geneId;

  private final String assayGroupId;

  private final double expressionLevel;

  private final double expressionLevelFpkm;

  private final double[] expressionLevels;

  private final double[] expressionLevelsFpkm;

  AutoValue_BaselineAnalytics(
      String geneId,
      String assayGroupId,
      double expressionLevel,
      double expressionLevelFpkm,
      double[] expressionLevels,
      double[] expressionLevelsFpkm) {
    if (geneId == null) {
      throw new NullPointerException("Null geneId");
    }
    this.geneId = geneId;
    if (assayGroupId == null) {
      throw new NullPointerException("Null assayGroupId");
    }
    this.assayGroupId = assayGroupId;
    this.expressionLevel = expressionLevel;
    this.expressionLevelFpkm = expressionLevelFpkm;
    if (expressionLevels == null) {
      throw new NullPointerException("Null expressionLevels");
    }
    this.expressionLevels = expressionLevels;
    if (expressionLevelsFpkm == null) {
      throw new NullPointerException("Null expressionLevelsFpkm");
    }
    this.expressionLevelsFpkm = expressionLevelsFpkm;
  }

  @Override
  public String geneId() {
    return geneId;
  }

  @Override
  public String assayGroupId() {
    return assayGroupId;
  }

  @Override
  public double expressionLevel() {
    return expressionLevel;
  }

  @Override
  public double expressionLevelFpkm() {
    return expressionLevelFpkm;
  }

  @SuppressWarnings(value = {"mutable"})
  @Override
  public double[] expressionLevels() {
    return expressionLevels;
  }

  @SuppressWarnings(value = {"mutable"})
  @Override
  public double[] expressionLevelsFpkm() {
    return expressionLevelsFpkm;
  }

  @Override
  public String toString() {
    return "BaselineAnalytics{"
         + "geneId=" + geneId + ", "
         + "assayGroupId=" + assayGroupId + ", "
         + "expressionLevel=" + expressionLevel + ", "
         + "expressionLevelFpkm=" + expressionLevelFpkm + ", "
         + "expressionLevels=" + Arrays.toString(expressionLevels) + ", "
         + "expressionLevelsFpkm=" + Arrays.toString(expressionLevelsFpkm)
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof BaselineAnalytics) {
      BaselineAnalytics that = (BaselineAnalytics) o;
      return (this.geneId.equals(that.geneId()))
           && (this.assayGroupId.equals(that.assayGroupId()))
           && (Double.doubleToLongBits(this.expressionLevel) == Double.doubleToLongBits(that.expressionLevel()))
           && (Double.doubleToLongBits(this.expressionLevelFpkm) == Double.doubleToLongBits(that.expressionLevelFpkm()))
           && (Arrays.equals(this.expressionLevels, (that instanceof AutoValue_BaselineAnalytics) ? ((AutoValue_BaselineAnalytics) that).expressionLevels : that.expressionLevels()))
           && (Arrays.equals(this.expressionLevelsFpkm, (that instanceof AutoValue_BaselineAnalytics) ? ((AutoValue_BaselineAnalytics) that).expressionLevelsFpkm : that.expressionLevelsFpkm()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= geneId.hashCode();
    h$ *= 1000003;
    h$ ^= assayGroupId.hashCode();
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(expressionLevel) >>> 32) ^ Double.doubleToLongBits(expressionLevel));
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(expressionLevelFpkm) >>> 32) ^ Double.doubleToLongBits(expressionLevelFpkm));
    h$ *= 1000003;
    h$ ^= Arrays.hashCode(expressionLevels);
    h$ *= 1000003;
    h$ ^= Arrays.hashCode(expressionLevelsFpkm);
    return h$;
  }

}

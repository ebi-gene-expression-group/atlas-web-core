

package uk.ac.ebi.atlas.experimentpage.tsne;

import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_TSnePoint_Dto extends TSnePoint.Dto {

  private final double x;

  private final double y;

  private final double expressionLevel;

  private final int clusterId;

  private final String name;

  AutoValue_TSnePoint_Dto(
      double x,
      double y,
      double expressionLevel,
      int clusterId,
      String name) {
    this.x = x;
    this.y = y;
    this.expressionLevel = expressionLevel;
    this.clusterId = clusterId;
    if (name == null) {
      throw new NullPointerException("Null name");
    }
    this.name = name;
  }

  @Override
  public double x() {
    return x;
  }

  @Override
  public double y() {
    return y;
  }

  @Override
  public double expressionLevel() {
    return expressionLevel;
  }

  @Override
  public int clusterId() {
    return clusterId;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return "Dto{"
         + "x=" + x + ", "
         + "y=" + y + ", "
         + "expressionLevel=" + expressionLevel + ", "
         + "clusterId=" + clusterId + ", "
         + "name=" + name
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TSnePoint.Dto) {
      TSnePoint.Dto that = (TSnePoint.Dto) o;
      return (Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x()))
           && (Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y()))
           && (Double.doubleToLongBits(this.expressionLevel) == Double.doubleToLongBits(that.expressionLevel()))
           && (this.clusterId == that.clusterId())
           && (this.name.equals(that.name()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(x) >>> 32) ^ Double.doubleToLongBits(x));
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(y) >>> 32) ^ Double.doubleToLongBits(y));
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(expressionLevel) >>> 32) ^ Double.doubleToLongBits(expressionLevel));
    h$ *= 1000003;
    h$ ^= clusterId;
    h$ *= 1000003;
    h$ ^= name.hashCode();
    return h$;
  }

}

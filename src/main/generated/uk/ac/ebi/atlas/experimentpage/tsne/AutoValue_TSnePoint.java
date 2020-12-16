

package uk.ac.ebi.atlas.experimentpage.tsne;

import java.util.Optional;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_TSnePoint extends TSnePoint {

  private final double x;

  private final double y;

  private final Optional<Double> expressionLevel;

  private final String name;

  private final String metadata;

  AutoValue_TSnePoint(
      double x,
      double y,
      Optional<Double> expressionLevel,
      String name,
      String metadata) {
    this.x = x;
    this.y = y;
    if (expressionLevel == null) {
      throw new NullPointerException("Null expressionLevel");
    }
    this.expressionLevel = expressionLevel;
    if (name == null) {
      throw new NullPointerException("Null name");
    }
    this.name = name;
    if (metadata == null) {
      throw new NullPointerException("Null metadata");
    }
    this.metadata = metadata;
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
  public Optional<Double> expressionLevel() {
    return expressionLevel;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String metadata() {
    return metadata;
  }

  @Override
  public String toString() {
    return "TSnePoint{"
         + "x=" + x + ", "
         + "y=" + y + ", "
         + "expressionLevel=" + expressionLevel + ", "
         + "name=" + name + ", "
         + "metadata=" + metadata
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TSnePoint) {
      TSnePoint that = (TSnePoint) o;
      return (Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x()))
           && (Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y()))
           && (this.expressionLevel.equals(that.expressionLevel()))
           && (this.name.equals(that.name()))
           && (this.metadata.equals(that.metadata()));
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
    h$ ^= expressionLevel.hashCode();
    h$ *= 1000003;
    h$ ^= name.hashCode();
    h$ *= 1000003;
    h$ ^= metadata.hashCode();
    return h$;
  }

}

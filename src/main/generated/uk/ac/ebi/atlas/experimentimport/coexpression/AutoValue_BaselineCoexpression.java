

package uk.ac.ebi.atlas.experimentimport.coexpression;

import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_BaselineCoexpression extends BaselineCoexpression {

  private final double ceStatistic;

  private final String ceGeneID;

  AutoValue_BaselineCoexpression(
      double ceStatistic,
      String ceGeneID) {
    this.ceStatistic = ceStatistic;
    if (ceGeneID == null) {
      throw new NullPointerException("Null ceGeneID");
    }
    this.ceGeneID = ceGeneID;
  }

  @Override
  public double ceStatistic() {
    return ceStatistic;
  }

  @Override
  public String ceGeneID() {
    return ceGeneID;
  }

  @Override
  public String toString() {
    return "BaselineCoexpression{"
         + "ceStatistic=" + ceStatistic + ", "
         + "ceGeneID=" + ceGeneID
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof BaselineCoexpression) {
      BaselineCoexpression that = (BaselineCoexpression) o;
      return (Double.doubleToLongBits(this.ceStatistic) == Double.doubleToLongBits(that.ceStatistic()))
           && (this.ceGeneID.equals(that.ceGeneID()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(ceStatistic) >>> 32) ^ Double.doubleToLongBits(ceStatistic));
    h$ *= 1000003;
    h$ ^= ceGeneID.hashCode();
    return h$;
  }

}

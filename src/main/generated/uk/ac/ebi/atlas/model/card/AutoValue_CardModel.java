

package uk.ac.ebi.atlas.model.card;

import java.util.List;
import java.util.Optional;
import javax.annotation.processing.Generated;
import org.apache.commons.lang3.tuple.Pair;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_CardModel extends CardModel {

  private final CardIconType iconType;

  private final String iconSrc;

  private final Pair<Optional<String>, Optional<String>> description;

  private final List<Pair<String, Optional<String>>> content;

  AutoValue_CardModel(
      CardIconType iconType,
      String iconSrc,
      Pair<Optional<String>, Optional<String>> description,
      List<Pair<String, Optional<String>>> content) {
    if (iconType == null) {
      throw new NullPointerException("Null iconType");
    }
    this.iconType = iconType;
    if (iconSrc == null) {
      throw new NullPointerException("Null iconSrc");
    }
    this.iconSrc = iconSrc;
    if (description == null) {
      throw new NullPointerException("Null description");
    }
    this.description = description;
    if (content == null) {
      throw new NullPointerException("Null content");
    }
    this.content = content;
  }

  @Override
  public CardIconType iconType() {
    return iconType;
  }

  @Override
  public String iconSrc() {
    return iconSrc;
  }

  @Override
  public Pair<Optional<String>, Optional<String>> description() {
    return description;
  }

  @Override
  public List<Pair<String, Optional<String>>> content() {
    return content;
  }

  @Override
  public String toString() {
    return "CardModel{"
         + "iconType=" + iconType + ", "
         + "iconSrc=" + iconSrc + ", "
         + "description=" + description + ", "
         + "content=" + content
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof CardModel) {
      CardModel that = (CardModel) o;
      return (this.iconType.equals(that.iconType()))
           && (this.iconSrc.equals(that.iconSrc()))
           && (this.description.equals(that.description()))
           && (this.content.equals(that.content()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= iconType.hashCode();
    h$ *= 1000003;
    h$ ^= iconSrc.hashCode();
    h$ *= 1000003;
    h$ ^= description.hashCode();
    h$ *= 1000003;
    h$ ^= content.hashCode();
    return h$;
  }

}

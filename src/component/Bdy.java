package component;

import base.Region;
import util.IntMap;

/**
 * Define the frame element -- bdy.
 */
public class Bdy {

  /**
   * Set if this {@code Bdy} additionally accepts {@code Itr} from teammate.
   * e.g., frozen state, Freeze's column.
   */
  public static final int FRIENDLY_FIREABLE = 0x1;

  /**
   * The relative {@code Region} of this {@code Bdy}.
   */
  public final Region relative;

  /**
   * Additional attributes of this {@code Bdy}.
   * It may cause different interaction to {@code Itr}s.
   */
  public final int attributes;

  private Bdy(Region relative, int attributes) {
    this.relative = relative;
    this.attributes = attributes;
  }

  /**
   * Creates a normal {@code Bdy} instance without attribute.
   *
   * @param relative {@code Region} indicating {@code Bdy}'s coverage
   * @return new instance of {@code Bdy}
   */
  public static Bdy of(Region relative) {
    return new Bdy(relative, 0);
  }

  /**
   * Creates a {@code Bdy} instance with given attributes.
   *
   * @param relative   {@code Region} indicating {@code Bdy}'s coverage
   * @param attributes the additional attributes
   * @return new instance of {@code Bdy}
   */
  public static Bdy of(Region relative, int attributes) {
    return new Bdy(relative, attributes);
  }

  @Override
  public String toString() {
    return String.format("Bdy[%s, attributes=%x]", relative, attributes);
  }

  // ==================== Parser Utility ====================

  /**
   * Extracts and prepares {@code Bdy} setting.
   *
   * @param data     a map containing key-value pairs
   * @param rawState original state of the enclosing frame
   * @return a statement to create a {@code Bdy}
   * @throws IllegalArgumentException for invalid kind
   */
  public static String extract(IntMap data, int rawState) {
    if (data.pop("kind") != 0) {
      throw new IllegalArgumentException("kind");
    }
    String region = Region.extract(data);
    if (rawState == 13) {
      return "Bdy.of(%s, %s)".formatted(region, "Bdy.FRIENDLY_FIREABLE");
    } else {
      return "Bdy.of(%s)".formatted(region);
    }
  }

}

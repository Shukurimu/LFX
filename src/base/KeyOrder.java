package base;

public enum KeyOrder {
  hit_ja ("dja"),
  hit_Ua ("dUa"),
  hit_Uj ("dUj"),
  hit_Da ("dDa"),
  hit_Dj ("dDj"),
  hit_Ra ("dRa"),
  hit_Rj ("dRj"),
  hit_La ("dLa"),
  hit_Lj ("dLj"),
  hit_a  ("a"),
  hit_j  ("j"),
  hit_d  ("d"),
  NONE   (".");

  /**
   * The representing key sequence of this {@code KeyOrder}.
   */
  public final String keySequence;

  private KeyOrder(String keySequence) {
    this.keySequence = keySequence;
  }

  @Override
  public String toString() {
    return String.join(".", getDeclaringClass().getSimpleName(), name());
  }

}

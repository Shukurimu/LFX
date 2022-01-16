package base;

import java.util.List;

public interface Controller {

  enum Input {
    Up     ("U"),
    Down   ("D"),
    Left   ("L"),
    Right  ("R"),
    Attack ("a"),
    Jump   ("j"),
    Defend ("d");

    public final String symbol;

    private Input(String symbol) {
      this.symbol = symbol;
    }

  }

  /**
   * Asks this {@code Controller} to retrieve the latest state.
   */
  void update();

  /**
   * Informs {@code Controller} the previous {@code KeyOrder} has
   * been processed, marking it as outdated.
   */
  void consume();

  /**
   * Gets the latest valid {@code KeyOrder}.
   *
   * @return a {@code KeyOrder}
   */
  KeyOrder getKeyOrder();

  /** Returns {@code true} if Up key is pressed. */
  boolean press_U();

  /** Returns {@code true} if Down key is pressed. */
  boolean press_D();

  /** Returns {@code true} if Left key is pressed. */
  boolean press_L();

  /** Returns {@code true} if Right key is pressed. */
  boolean press_R();

  /** Returns {@code true} if Attack key is pressed. */
  boolean press_a();

  /** Returns {@code true} if Jump key is pressed. */
  boolean press_j();

  /** Returns {@code true} if Defend key is pressed. */
  boolean press_d();

  /** Returns {@code true} if Running is triggered. */
  boolean pressRun();

  /** Returns {@code true} if x direction move is triggered. */
  default boolean pressX() {
    return press_L() ^ press_R();
  }

  /** Returns {@code true} if z direction move is triggered. */
  default boolean pressZ() {
    return press_U() ^ press_D();
  }

  /** Returns {@code true} if a move is triggered. */
  default boolean pressWalk() {
    return pressX() || pressZ();
  }

  /**
   * Returns the signum function of current state;
   * +1.0 if effective right,
   * -1.0 if effective left,
   * and 0.0 otherwise.
   *
   * @return the signum function
   */
  default double valueX() {
    return press_L() ? (press_R() ? 0.0 : -1.0) : (press_R() ? 1.0 : 0.0);
  }

  /**
   * Returns the signum function of current state;
   * +1.0 if effective up,
   * -1.0 if effective down,
   * and 0.0 otherwise.
   *
   * @return the signum function
   */
  default double valueZ() {
    return press_D() ? (press_U() ? 0.0 : -1.0) : (press_U() ? 1.0 : 0.0);
  }

  /**
   * Returns the final facing direction under current state.
   *
   * @param originalFacing the original direction
   * @return {@code true} if right side and {@code false} if left side
   */
  default boolean getFacing(boolean originalFacing) {
    return press_L() ? (press_R() && originalFacing)
                     : (press_R() || originalFacing);
  }

  /**
   * Checks if {@code Controller} causes direction change.
   *
   * @param originalFacing the original direction
   * @return {@code true} if one will face to opposide direction
   */
  default boolean reverseFacing(boolean originalFacing) {
    return press_L() ? (!press_R() && originalFacing)
                     : (press_R() && !originalFacing);
  }

  /**
   * A no-source {@code Controller} which is never updated
   * and therefore no {@code KeyOrder}.
   */
  Controller NULL_CONTROLLER = new Controller() {
    @Override public void update() {}
    @Override public void consume() {}
    @Override public KeyOrder getKeyOrder() { return KeyOrder.NONE; }
    @Override public boolean press_U() { return false; }
    @Override public boolean press_D() { return false; }
    @Override public boolean press_L() { return false; }
    @Override public boolean press_R() { return false; }
    @Override public boolean press_a() { return false; }
    @Override public boolean press_j() { return false; }
    @Override public boolean press_d() { return false; }
    @Override public boolean pressRun() { return false; }
    @Override public boolean pressX() { return false; }
    @Override public boolean pressZ() { return false; }
    @Override public boolean pressWalk() { return false; }
    @Override public double valueX() { return 0.0; }
    @Override public double valueZ() { return 0.0; }
    @Override public boolean getFacing(boolean originalFacing) { return true; }
    @Override public boolean reverseFacing(boolean originalFacing) { return false; }
  };

  static Controller union(List<Controller> group) {
    return new Controller() {
      @Override public void consume() { group.forEach(c -> c.consume()); }
      @Override public void update() { group.forEach(c -> c.update()); }
      @Override public KeyOrder getKeyOrder() { return KeyOrder.NONE; }
      @Override public boolean press_U() { return group.stream().anyMatch(c -> c.press_U()); }
      @Override public boolean press_D() { return group.stream().anyMatch(c -> c.press_D()); }
      @Override public boolean press_L() { return group.stream().anyMatch(c -> c.press_L()); }
      @Override public boolean press_R() { return group.stream().anyMatch(c -> c.press_R()); }
      @Override public boolean press_a() { return group.stream().anyMatch(c -> c.press_a()); }
      @Override public boolean press_j() { return group.stream().anyMatch(c -> c.press_j()); }
      @Override public boolean press_d() { return group.stream().anyMatch(c -> c.press_d()); }
      @Override public boolean pressRun() { return false; }
    };
  }

}

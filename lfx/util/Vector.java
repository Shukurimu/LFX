package lfx.util;

public class Vector {
  protected double px = 0.0;
  protected double py = 0.0;
  protected double pz = 0.0;
  protected double vx = 0.0;
  protected double vy = 0.0;
  protected double vz = 0.0;
  protected boolean faceRight = true;

  public Vector() {
    // (empty)
  }

  public final double[] getPosition() {
    return new double[] {px, py, pz}
  }

  public final boolean getFacing() {
    return faceRight;
  }

  public final void move(double dx, double dy, double dz) {
    px += dx;
    py += dy;
    pz += dz;
    return;
  }

}

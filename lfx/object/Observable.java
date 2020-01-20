package lfx.object;

import java.util.List;

public interface Observable {

  /** Get picture-rendering information. */
  public Vector getVector();

  /** Called by AbstractMap. */
  public void initialize(double px, double py, double pz, int actNumber);

  /** Called when F7 is pressed. */
  public void revive();

  protected void registerBdyArea();
  protected void registerItrArea();
  public List<Tuple<Bdy, Area>> getBdyAreaList();
  public List<Tuple<Itr, Area>> getItrAreaList();

  public void applyItrArea(int mapTime, List<Observable> observableList);
  public void reactAndMove();
  public boolean validate

}

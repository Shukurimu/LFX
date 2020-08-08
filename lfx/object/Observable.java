package lfx.object;

import java.util.List;
import lfx.base.Viewer;
import lfx.component.Bdy;
import lfx.component.Frame;
import lfx.component.Itr;
import lfx.map.Environment;
import lfx.util.Area;
import lfx.util.Point;
import lfx.util.Tuple;

public interface Observable {

  Observable makeClone();
  String getIdentifier();
  int getTeamId();
  double getPosX();
  boolean getFacing();
  boolean isFirstTimeunit();
  boolean isActionFirstTimeunit();
  Frame getCurrentFrame();

  /**
   * Returns the sign of value representing Up/Down key press.
   * For opoint and throwing, they can have initial vz.
   *
   * @return +1, 0, or -1
   */
  double getInputZ();

  /**
   * Resets the object to its initial stamina, basically called when F7 is pressed.
   */
  void revive();

  /** For Xpoint to set correct position. */
  List<Double> getBasePosition(Point point);
  /** Set position of Xpoint relative to base position. */
  void setPosition(List<Double> basePosition, Point point, double zOffect);
  void setPosition(double px, double py, double pz);
  /** Used in Opoint. */
  void setVelocity(double vx, double vy, double vz);
  void setProperty(Environment env, int teamId, boolean faceRight);

  List<Tuple<Bdy, Area>> getBdys();

  List<Tuple<Itr, Area>> getItrs();

  /**
   * Returns this scope from another's perspective.
   * It is mainly used while checking interaction.
   */
  int getScopeView(int targetTeamId);
  /** Check all ItrArea happened in current timeunit.
      Store the interactable ones into pending list. */
  void spreadItrs(Iterable<Observable> everything);

  void receiveItr(Observable source, Itr itr);

  /** React to the received ItrArea. Postback actLag. */
  void react();

  /** Do actions & update status. */
  void act();

  List<Observable> getSpawnedObjectList();

  boolean exists();

  Viewer getViewer();

}

// https://stackoverflow.com/questions/56867/interface-vs-base-class
// http://gjp4860sev.myweb.hinet.net/lf2/page10.htm
// https://lf-empire.de/lf2-empire/data-changing/frame-elements/177-cpoint-catch-point?showall=1

/*
  protected int graspField = 0;
  public static final int GRASP_TIME = 305;  // test-value
  public static final int GRASP_FLAG_WAITING = -1;
  public static final int GRASP_FLAG_UPDATED = -2;
  public static final int GRASP_FLAG_FREE = -3;
  public static final int GRASP_FLAG_DROP = -4;
  public static final int GRASP_FLAG_THROW = -5;
  public static final double GRASP_DROP_DVX = +8.0;
  public static final double GRASP_DROP_DVY = -2.5;
  protected AbstractObject grasper = dummy;
  protected AbstractObject graspee = dummy;
  protected synchronized int updateGraspee() {
    while (graspField == GRASP_FLAG_WAITING) {
      try {
        this.wait(1000);
      } catch (InterruptedException expected) {
      }
    }
    if (graspField == GRASP_FLAG_FREE)
      return ACT_JUMPAIR;
    if (graspField == GRASP_FLAG_DROP)
      return ACT_FORWARD_FALL2;
    final Cpoint cpoint = grasper.currFrame.cpoint;
    if (graspField == GRASP_FLAG_THROW) {
      vx = grasper.faceRight ? cpoint.throwvx : -cpoint.throwvx;
      vy = cpoint.throwvy;
      vz = catcher.getControlZ() * cpoint.throwvz;
      status.put(Extension.Kind.THROWINJURY, new Extension(-1, cpoint.throwinjury));
      return cpoint.vaction;
    }
    if (cpoint.injury > 0) {
      hpLost(cpoint.injury, false);
      actLag = Math.max(actLag, Itr.LAG);
    } else {
      hpLost(-cpoint.injury, false);
    }
    faceRight = grasper.faceRight ^ cpoint.changedir;
    px = grasper.faceRight ?
         (grasper.anchorX + cpoint.x) + (currFrame.cpoint.x - currFrame.centerx):
         (grasper.anchorX - cpoint.x) - (currFrame.cpoint.x - currFrame.centerx);
    py = (grasper.anchorY + cpoint.y) - (currFrame.cpoint.y - currFrame.centery);
    pz = grasper.pz;
    graspField = GRASP_FLAG_WAITING;
    return actLag == 0 ? cpoint.vaction : ACT_TBA;
  }


  protected final int updateGrasp() {
    if (grasper.currFrame.state != State.GRASP || grasper.currFrame.cpoint == null ||
        graspee.currFrame.state != State.GRASP || graspee.currFrame.cpoint == null) {
      return ACT_DEF;
    }
    if (grasper == this) {
      if (graspee.grasper != this) {
        // re-grasped by other
        grasper = graspee = dummy;
        return ACT_DEF;
      }
      return updateGrasper();
    } else {
      return updateGraspee();
    }
  }

  private int updateGrasper() {
    int graspeeFlag = GRASP_FLAG_WAITING;
    graspField -= Math.abs(cpoint.decrease);
    if (currFrame.cpoint == null) {
      // does a combo and goes to a frame without cpoint
      graspeeFlag = GRASP_FLAG_FREE;
    } else if (graspField < 0 && currFrame.cpoint.decrease < 0) {
      // will not drop graspee in cpoint with positive decrease even if timeup
      graspeeFlag = GRASP_FLAG_DROP;
    } else if (transition == currFrame.wait) {
      // these functions only take effect once
        if (currFrame.cpoint.injury > 0) {
          actLag = Math.max(actLag, Itr.LAG);
        }
        if (cpoint.throwing) {
          graspeeFlag = GRASP_FLAG_THROW;
        }
        if (cpoint.transform) {
          graspeeFlag = GRASP_FLAG_THROW;
          status.put(Extension.Kind.TRANSFORM_TO, new Extension(1, graspee.identifier));
        }
      }
    }
    graspee.setPointPosition(getPointPosition(currFrame.cpoint), graspee.currFrame.cpoint);
    synchronized (graspee) {
      graspee.graspField = graspeeFlag;
      graspee.notify();
    }
    return;
  }
*/

package lfx.map;

import java.util.stream;
import lfx.object.AbstractObject;
import lfx.util.Observable;

public interface Environment {

  public double applyFriction(double vx);

  public List<Observable> getObservableList();

  public void registerSpawnedObservable(List<Observable> spawnedObservableList);

}

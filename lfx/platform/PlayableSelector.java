package lfx.platform;

import java.util.List;
import lfx.game.Playable;
import lfx.util.Selectable;

public class PlayableSelector implements Selectable<Playable> {
  private final List<Playable> valueList = List.of(
    Playable.SELECTION_RANDOM
  );
  private Playable current = Playable.SELECTION_RANDOM;

  @Override
  public Playable get() {
    return current;
  }

  @Override
  public void setPrevious() {
    int oldIndex = valueList.indexOf(current);
    int newIndex = valueList.size() + oldIndex - 1;
    current = valueList.get(newIndex % valueList.size());
    return;
  }

  @Override
  public void setNext() {
    int oldIndex = valueList.indexOf(current);
    int newIndex = oldIndex + 1;
    current = valueList.get(newIndex % valueList.size());
    return;
  }

  @Override
  public void setDefault() {
    current = Playable.SELECTION_RANDOM;
    return;
  }

}

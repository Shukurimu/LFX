package platform;

import java.util.ArrayList;
import java.util.List;
import game.Library;
import game.Playable;
import util.Selectable;

public class PlayableSelector implements Selectable<Playable> {
  private final List<Playable> valueList = new ArrayList<>(40);
  private Playable current = Playable.SELECTION_RANDOM;

  public PlayableSelector() {
    valueList.add(Playable.SELECTION_RANDOM);
    valueList.addAll(Library.instance().getPlayableList());
  }

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

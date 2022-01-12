package base;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseController implements Controller {
  private static final long VALID_PREPRESS_MS = 100L;
  private static final long VALID_INTERVAL_MS = 200L;
  private static final KeyOrder[] keyOrderArray = KeyOrder.values();

  private final InputMonitor monitorUp;
  private final InputMonitor monitorDown;
  private final InputMonitor monitorLeft;
  private final InputMonitor monitorRight;
  private final InputMonitor monitorAttack;
  private final InputMonitor monitorJump;
  private final InputMonitor monitorDefend;
  private final List<Map.Entry<Input, InputMonitor>> inputSequenceBuffer;
  private Instant validComboFrom = Instant.EPOCH;
  private Instant validFrom = Instant.MAX;
  private Instant validTo = Instant.MAX;

  public BaseController(Map<Input, InputMonitor> inputMap) {
    monitorUp = inputMap.get(Input.Up);
    monitorDown = inputMap.get(Input.Down);
    monitorLeft = inputMap.get(Input.Left);
    monitorRight = inputMap.get(Input.Right);
    monitorAttack = inputMap.get(Input.Attack);
    monitorJump = inputMap.get(Input.Jump);
    monitorDefend = inputMap.get(Input.Defend);
    inputSequenceBuffer = new ArrayList<>(inputMap.entrySet());
  }

  @Override
  public void consume() {
    validComboFrom = Instant.now();
    return;
  }

  @Override
  public void update() {
    Instant now = Instant.now();
    validFrom = now.minusMillis(VALID_PREPRESS_MS);
    validTo = now.plusMillis(VALID_INTERVAL_MS);
    return;
  }

  @Override
  public KeyOrder getKeyOrder() {
    StringBuilder sequence = new StringBuilder(8);
    inputSequenceBuffer.sort((e1, e2) -> e1.getValue().compareTo(e2.getValue()));
    for (Map.Entry<Input, InputMonitor> entry : inputSequenceBuffer) {
      if (entry.getValue().pressedAfter(validComboFrom)) {
        sequence.append(entry.getKey().symbol);
      }
    }
    for (KeyOrder order : keyOrderArray) {
      if (sequence.indexOf(order.keySequence) >= 0) {
        return order;
      }
    }
    return null;
  }

  @Override
  public boolean press_U() {
    return monitorUp.isPressed();
  }

  @Override
  public boolean press_D() {
    return monitorDown.isPressed();
  }

  @Override
  public boolean press_L() {
    return monitorLeft.isPressed();
  }

  @Override
  public boolean press_R() {
    return monitorRight.isPressed();
  }

  @Override
  public boolean press_a() {
    return monitorAttack.pressedBetween(validFrom, validTo);
  }

  @Override
  public boolean press_j() {
    return monitorJump.pressedBetween(validFrom, validTo);
  }

  @Override
  public boolean press_d() {
    return monitorDefend.pressedBetween(validFrom, validTo);
  }

  public boolean pressRunL() {
    return monitorLeft.pressedBetween(validFrom, validTo)
        && monitorLeft.isDoublePressed(VALID_INTERVAL_MS);
  }

  public boolean pressRunR() {
    return monitorRight.pressedBetween(validFrom, validTo)
        && monitorRight.isDoublePressed(VALID_INTERVAL_MS);
  }

  @Override
  public boolean pressRun() {
    return pressRunL() ^ pressRunR();
  }

}

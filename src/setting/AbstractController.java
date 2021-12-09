package setting;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import base.Controller;
import base.Order;
import setting.Input;
import util.Tuple;

public abstract class AbstractController implements Controller {
  private static final long VALID_PREPRESS_MS = 100L;
  private static final long VALID_INTERVAL_MS = 200L;
  private final List<Tuple<InputMonitor, String>> inputSequenceBuffer = new ArrayList<>(8);
  private final Map<Input, InputMonitor> inputMap;
  private Instant validComboFrom = Instant.EPOCH;
  private Instant validFrom = Instant.MAX;
  private Instant validTo = Instant.MAX;

  protected AbstractController(Map<Input, InputMonitor> inputMap) {
    this.inputMap = inputMap;
    for (Map.Entry<Input, InputMonitor> entry : inputMap.entrySet()) {
      inputSequenceBuffer.add(new Tuple<>(entry.getValue(), entry.getKey().symbol));
    }
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
  public Order getOrder() {
    StringBuilder sequence = new StringBuilder(8);
    inputSequenceBuffer.sort((e1, e2) -> e1.first.compareTo(e2.first));
    for (Tuple<InputMonitor, String> tuple : inputSequenceBuffer) {
      if (tuple.first.pressedAfter(validComboFrom)) {
        sequence.append(tuple.second);
      }
    }
    for (Order order : Order.ORDER_LIST) {
      if (sequence.indexOf(order.keySequence) >= 0) {
        return order;
      }
    }
    return null;
  }

  @Override
  public boolean press_U() {
    return inputMap.get(Input.Up).isPressed();
  }

  @Override
  public boolean press_D() {
    return inputMap.get(Input.Down).isPressed();
  }

  @Override
  public boolean press_L() {
    return inputMap.get(Input.Left).isPressed();
  }

  @Override
  public boolean press_R() {
    return inputMap.get(Input.Right).isPressed();
  }

  @Override
  public boolean press_a() {
    InputMonitor monitor = inputMap.get(Input.Attack);
    return monitor.pressedBetween(validFrom, validTo);
  }

  @Override
  public boolean press_j() {
    InputMonitor monitor = inputMap.get(Input.Jump);
    return monitor.pressedBetween(validFrom, validTo);
  }

  @Override
  public boolean press_d() {
    InputMonitor monitor = inputMap.get(Input.Defend);
    return monitor.pressedBetween(validFrom, validTo);
  }

  public boolean pressRunL() {
    InputMonitor monitor = inputMap.get(Input.Left);
    return monitor.pressedBetween(validFrom, validTo)
        && monitor.isDoublePressed(VALID_INTERVAL_MS);
  }

  public boolean pressRunR() {
    InputMonitor monitor = inputMap.get(Input.Right);
    return monitor.pressedBetween(validFrom, validTo)
        && monitor.isDoublePressed(VALID_INTERVAL_MS);
  }

  @Override
  public boolean pressRun() {
    return pressRunL() ^ pressRunR();
  }

}

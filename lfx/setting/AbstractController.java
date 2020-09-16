package lfx.setting;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lfx.base.Controller;
import lfx.base.Order;
import lfx.setting.Input;
import lfx.util.Tuple;

public abstract class AbstractController implements Controller {
  private static final long VALID_INTERVAL_MS = 200L;
  private final List<Tuple<InputMonitor, String>> inputSequenceBuffer = new ArrayList<>(8);
  private final Map<Input, InputMonitor> inputMap;
  private Instant validFrom = Instant.EPOCH;
  private Instant validTo = Instant.EPOCH;

  protected AbstractController(Map<Input, InputMonitor> inputMap) {
    this.inputMap = inputMap;
    for (Map.Entry<Input, InputMonitor> entry : inputMap.entrySet()) {
      inputSequenceBuffer.add(new Tuple<>(entry.getValue(), entry.getKey().symbol));
    }
  }

  @Override
  public void consume() {
    validFrom = Instant.now();
    return;
  }

  @Override
  public void update() {
    validTo = Instant.now().plusMillis(VALID_INTERVAL_MS);
    return;
  }

  @Override
  public Order getOrder() {
    StringBuilder sequence = new StringBuilder(8);
    inputSequenceBuffer.sort((e1, e2) -> e1.first.compareTo(e2.first));
    for (Tuple<InputMonitor, String> tuple : inputSequenceBuffer) {
      if (tuple.first.pressedAfter(validFrom)) {
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
    return monitor.pressedAfter(validFrom) && monitor.pressedBefore(validTo);
  }

  @Override
  public boolean press_j() {
    InputMonitor monitor = inputMap.get(Input.Jump);
    return monitor.pressedAfter(validFrom) && monitor.pressedBefore(validTo);
  }

  @Override
  public boolean press_d() {
    InputMonitor monitor = inputMap.get(Input.Defend);
    return monitor.pressedAfter(validFrom) && monitor.pressedBefore(validTo);
  }

  public boolean pressRunL() {
    InputMonitor monitor = inputMap.get(Input.Left);
    return monitor.isDoublePressed(VALID_INTERVAL_MS) && monitor.pressedBefore(validTo);
  }

  public boolean pressRunR() {
    InputMonitor monitor = inputMap.get(Input.Right);
    return monitor.isDoublePressed(VALID_INTERVAL_MS) && monitor.pressedBefore(validTo);
  }

  @Override
  public boolean pressRun() {
    return pressRunL() ^ pressRunR();
  }

}

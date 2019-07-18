import javafx.scene.input.KeyCode;

final class LFkeyrecord implements Comparable<LFkeyrecord> {
    public static final int NONE = 0;
    public static final int HOLDING = 0b01;
    public static final int DBPRESS = 0b10;
    public static final long pressInterval = 200L;
    public static final LFkeyrecord guard = new LFkeyrecord();
    
    public long pressTime = 0L;
    private int pressCount = 0;
    private boolean doublePressed = false;
    
    public LFkeyrecord() {}
    
    public void setPressed() {
        if (++pressCount == 1) {
            long systemTime = System.currentTimeMillis();
            doublePressed = (pressTime + pressInterval > systemTime);
            pressTime = systemTime;
        }
        return;
    }
    
    public void setReleased() {
        pressCount = 0;
        return;
    }
    
    public int getState() {
        doublePressed &= (pressTime + pressInterval > LFX.systemTime);
        return ((pressCount > 0) ? HOLDING : NONE) | (doublePressed ? DBPRESS : NONE);
    }
    
    public boolean isHolding() {
        return (pressCount > 0);
    }
    
    public boolean isValidPress() {
        return (pressTime + pressInterval > LFX.systemTime);
    }
    
    @Override
    public int compareTo(LFkeyrecord k) {
        return (pressTime > k.pressTime) ? 1 : -1;
    }
    
}


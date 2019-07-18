import java.util.Arrays;
import java.util.EnumMap;
import javafx.scene.input.KeyCode;

final class LFcontrol {
    public static final LFcontrol noControl = new LFcontrol();
    
    public final KeyCode code_U;
    public final KeyCode code_D;
    public final KeyCode code_L;
    public final KeyCode code_R;
    public final KeyCode code_a;
    public final KeyCode code_j;
    public final KeyCode code_d;
    
    private LFkeyrecord key_U = new LFkeyrecord();
    private LFkeyrecord key_D = new LFkeyrecord();
    private LFkeyrecord key_L = new LFkeyrecord();
    private LFkeyrecord key_R = new LFkeyrecord();
    private LFkeyrecord key_a = new LFkeyrecord();
    private LFkeyrecord key_j = new LFkeyrecord();
    private LFkeyrecord key_d = new LFkeyrecord();
    private final LFkeyrecord[] inputArrange = new LFkeyrecord[7];
    
    public final boolean activated;
    public boolean do_a  = false;
    public boolean do_j  = false;
    public boolean do_d  = false;
    public boolean do_R  = false;
    public boolean do_L  = false;
    public boolean do_U  = false;
    public boolean do_D  = false;
    public boolean do_F  = false;
    public boolean do_Z  = false;
    public boolean do_RR = false;
    public boolean do_LL = false;
    public LFact combo = LFact.NOP;
    private long lastClearTime = 0L;
    
    private LFcontrol() {
        activated = false;
        code_U = code_D = code_L = code_R = code_a = code_j = code_d = null;
    }
    
    public LFcontrol(String keySetting) {
        activated = true;
        String[] keyArray = Arrays.copyOf(keySetting.split(" "), 7);
        code_U = getOrUndefined(keyArray[0]);
        code_D = getOrUndefined(keyArray[1]);
        code_L = getOrUndefined(keyArray[2]);
        code_R = getOrUndefined(keyArray[3]);
        code_a = getOrUndefined(keyArray[4]);
        code_j = getOrUndefined(keyArray[5]);
        code_d = getOrUndefined(keyArray[6]);
        return;
    }
    
    /* invoked when a new LFmap is created */
    public void register(EnumMap<KeyCode, LFkeyrecord> mapKeyStatus) {
        lastClearTime = 1L;
        mapKeyStatus.putIfAbsent(code_U, new LFkeyrecord());
        inputArrange[0] = key_U = mapKeyStatus.get(code_U);
        mapKeyStatus.putIfAbsent(code_D, new LFkeyrecord());
        inputArrange[1] = key_D = mapKeyStatus.get(code_D);
        mapKeyStatus.putIfAbsent(code_L, new LFkeyrecord());
        inputArrange[2] = key_L = mapKeyStatus.get(code_L);
        mapKeyStatus.putIfAbsent(code_R, new LFkeyrecord());
        inputArrange[3] = key_R = mapKeyStatus.get(code_R);
        mapKeyStatus.putIfAbsent(code_a, new LFkeyrecord());
        inputArrange[4] = key_a = mapKeyStatus.get(code_a);
        mapKeyStatus.putIfAbsent(code_j, new LFkeyrecord());
        inputArrange[5] = key_j = mapKeyStatus.get(code_j);
        mapKeyStatus.putIfAbsent(code_d, new LFkeyrecord());
        inputArrange[6] = key_d = mapKeyStatus.get(code_d);
        return;
    }
    
    /* invoked per TimeUnit to get user input */
    public void updateInput() {
        if (lastClearTime > key_d.pressTime) {
            combo = LFact.NOP;
        } else {
            Arrays.sort(inputArrange);
            int dIndex = 0;
            for ( ; dIndex < 5; ++dIndex)
                if (inputArrange[dIndex] == key_d)
                    break;
            if (dIndex >= 5) {
                combo = LFact.NOP;
            } else {
                LFkeyrecord key2 = inputArrange[dIndex + 1];
                LFkeyrecord key3 = inputArrange[dIndex + 2];
                if (key2 == key_j)
                    combo = (key3 == key_a) ? LFact.hit_ja : LFact.NOP;
                else if (key2 == key_U)
                    combo = (key3 == key_a) ? LFact.hit_Ua : ((key3 == key_j) ? LFact.hit_Uj : LFact.NOP);
                else if (key2 == key_D)
                    combo = (key3 == key_a) ? LFact.hit_Da : ((key3 == key_j) ? LFact.hit_Dj : LFact.NOP);
                else if (key2 == key_L)
                    combo = (key3 == key_a) ? LFact.hit_La : ((key3 == key_j) ? LFact.hit_Lj : LFact.NOP);
                else if (key2 == key_R)
                    combo = (key3 == key_a) ? LFact.hit_Ra : ((key3 == key_j) ? LFact.hit_Rj : LFact.NOP);
                else
                    combo = LFact.NOP;
            }
        }
        do_RR = ((key_R.getState() & LFkeyrecord.DBPRESS) != 0);
        do_R  = ((key_R.getState() & LFkeyrecord.HOLDING) != 0);
        do_LL = ((key_L.getState() & LFkeyrecord.DBPRESS) != 0);
        do_L  = ((key_L.getState() & LFkeyrecord.HOLDING) != 0);
        do_U = key_U.isHolding();
        do_D = key_D.isHolding();
        do_a = key_a.isValidPress();
        do_j = key_j.isValidPress();
        do_d = key_d.isValidPress();
        do_F = do_R ^ do_L;
        do_Z = do_U ^ do_D;
        return;
    }
    
    public void consumeKey(LFact c) {
        lastClearTime = LFX.systemTime;
        return;
    }
    
    public String[] asStringArray() {
        return new String[] {
            code_U.toString(),
            code_D.toString(),
            code_L.toString(),
            code_R.toString(),
            code_a.toString(),
            code_j.toString(),
            code_d.toString()
        };
    }
    
    @Override
    public String toString() {
        return String.join(" ", asStringArray());
    }
    
    public static KeyCode getOrUndefined(String s) {
        KeyCode c = KeyCode.UNDEFINED;
        try {
            c = KeyCode.valueOf(s);
        } catch (Exception e) {
            System.err.println("Invalid KeyCode: " + s);
        }
        return c;
    }
    
}

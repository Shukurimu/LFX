
enum LFeffect {
    /* ExplosionType CauseHitLag NormalType */
    WPSTREN  (false, false, false, ""),
    VORTEX   (false, false, false, ""),
    LETSP    (false, false, false, ""),
    FENCE    (false, false, false, ""),
    HEAL     (false, false, false, ""),
    PICKSTAND(false, false, false, ""),
    PICKROLL (false, false, false, ""),
    GRASPDOP (false, false, false, ""),
    GRASPBDY (false, false, false, ""),
    
    SONATA  (false, false, false, ""),
    REFLECT (false,  true,  true, ""),
    FALLING (false,  true,  true, ""),
    SILENT  (false,  true,  true, ""),
    
    NONE    (false,  true,  true, ""),
    OTHER   (false,  true,  true, ""),
    PUNCH   (false,  true,  true, "data/001.wav"),
    STAB    (false,  true,  true, "data/032.wav"),
    FIRE    (false,  true,  true, "data/070.wav"),
    ICE     (false,  true,  true, "data/065.wav"),
    EXPLO   ( true,  true,  true, ""),
    EXFIRE  ( true,  true,  true, "data/070.wav"),
    EXICE   ( true,  true,  true, "data/065.wav"),
    FIRE2   (false,  true,  true, "data/070.wav"),
    ICE2    (false,  true,  true, "data/065.wav"),
    SPICE   (false, false,  true, "data/065.wav"),
    SPFIRE  (false, false,  true, "data/070.wav");
    
    public final boolean explosionType;
    public final boolean causeLag;
    public final boolean normalEffect;
    public final String sound;
    
    private LFeffect(boolean e, boolean l, boolean n, String w) {
        explosionType = e;
        causeLag = l;
        normalEffect = n;
        sound = w;
    }
    
    public String parserText() {
        return "LFeffect." + this.toString();
    }
    
    public static String state18(String originalScope, boolean is18) {
        if (!is18) return originalScope;
        char[] newScope = originalScope.toCharArray();
        newScope[3] = (newScope[2] == '1') ? '1' : newScope[3];
        newScope[5] = (newScope[4] == '1') ? '1' : newScope[5];
        newScope[7] = (newScope[6] == '1') ? '1' : newScope[7];
        return String.valueOf(newScope);
    }
    
    public static String[] parserKindMap(int originalKind, int originalEffect, int originalState) {
        boolean is18 = originalState == 18;
        switch (originalKind) {
            case 0:
                switch (originalEffect) {
                    case 1:
                        return new String[] { STAB.parserText(),   state18("0b101110", is18) };
                    case 2:
                        return new String[] {/* State19 Effect2 works as same as Effect20 (IMO) */
            ((originalState == 19) ? FIRE2 : FIRE).parserText(),   state18("0b101110", is18) };
                    case 20:
                        return new String[] { FIRE2.parserText(),  state18("0b001110", is18) };
                    case 21:
                        return new String[] { FIRE2.parserText(),  state18("0b101110", false) };
                    case 22:
                        return new String[] { EXFIRE.parserText(), state18("0b101110", false) };
                    case 23:
                        return new String[] { EXPLO.parserText(),  state18("0b101110", is18) };
                    case 3:
                        return new String[] { ICE.parserText(),    state18("0b101110", is18) };
                    case 30:
                        return new String[] { ICE2.parserText(),   state18("0b101110", is18) };
                    case 4:
                        return new String[] { PUNCH.parserText(),  state18("0b111100", is18) };
                    default:
                        return new String[] { OTHER.parserText(),  state18("0b101110", is18) };
                }
            case 4:
                return new String[] { FALLING.parserText(),  state18("0b111101", is18) };
            case 9:
                return new String[] { REFLECT.parserText(),  state18("0b111110", is18) };
            case 10:
            case 11:
                return new String[] { SONATA.parserText(),   state18("0b001110", is18) };
            case 16:
                return new String[] { SPICE.parserText(),    state18("0b000010", is18) };
            case 8:
                return new String[] { HEAL.parserText(),     state18("0b000011", is18) };
            case 1:
                return new String[] { GRASPDOP.parserText(), state18("0b000010", is18), "CatchType" };
            case 3:
                return new String[] { GRASPBDY.parserText(), state18("0b000010", is18), "CatchType" };
            case 2:
                return new String[] { PICKSTAND.parserText(),state18("0b001100", is18), "StrongType" };
            case 7:
                return new String[] { PICKROLL.parserText(), state18("0b001100", is18), "StrongType" };
            case 6:
                return new String[] { LETSP.parserText(),    state18("0b000010", is18), "StrongType" };
            case 14:
                return new String[] { FENCE.parserText(),    state18("0b111111", is18), "StrongType" };
            case 15:
                return new String[] { VORTEX.parserText(),   state18("0b001110", is18), "StrongType" };
            case 5:
                return new String[] { WPSTREN.parserText(),  state18("0b000000", is18), "StrongType" };
            default:
                System.out.printf("\tUnknown kind %s\n", originalKind);
                return null;
        }
    }
    
}

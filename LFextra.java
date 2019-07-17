
final class LFextra implements Cloneable {
    enum Kind {
        LANDING,
        TRANSFORM_TO,
        TRANSFORM_BACK,
        TELEPORT_ENEMY,
        TELEPORT_TEAM,
        HEALING,
        REGENERATION,
        INVISIBLE,
        SONATA,
        ARMOUR,
        THROWINJURY,
        LETSPUNCH,
        MOVEBLOCK;
        
    }
    
    /* this class is to make special moves more functional
       for instance, healing faster (higher rate > 1.0), or landing to other than Frame94
       or even posion attack (I have heard in some DC version)
       you have to deal with each case in LFobject.updateStatus() switch conditions */
    private static final LFextra ONE_TIME = new LFextra(1, 0, 0.0, "");
    private int timeLeft;
    public final int intValue;// e.g., landing frame
    public final double doubleValue;// e.g., healing rate
    public final String stringValue;// e.g., transform-target identifier
    
    public LFextra(int tl, int iv, double dv, String sv) {
        timeLeft = tl;
        intValue = iv;
        doubleValue = dv;
        stringValue = sv;
    }
    
    public LFextra(int tl) {
        this(tl,  0, 0.0, "");
    }
    
    public LFextra(int tl, int iv) {
        this(tl, iv, 0.0, "");
    }
    
    public LFextra(int tl, double dv) {
        this(tl,  0,  dv, "");
    }
    
    public LFextra(int tl, String sv) {
        this(tl,  0, 0.0, sv);
    }
    
    @Override
    protected LFextra clone() {
        try {
            return (LFextra)super.clone();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /* temporarily replace by new one */
    public LFextra stack(Kind k, LFextra e) {
        return this.clone();
    }
    
    /* the extra effect will be removed if this method returns true, therefore
       you can set timeLeft<0 for those condition-based actions (e.g., special langing) */
    public boolean lapse() {
        return --timeLeft == 0;
    }
    
    public static LFextra oneTime() {
        return ONE_TIME.clone();
    }
    
    public static String parserState(int originalState) {
        switch (originalState) {
            case 9996:
                return "LFextra.Kind.ARMOUR, LFextra.oneTime()";
            case 1700:
                return "LFextra.Kind.HEALING, new LFextra(100, 1.0)";
            case 400:
                return "LFextra.Kind.TELEPORT_ENEMY, new LFextra(1, 120.0)";
            case 401:
                return "LFextra.Kind.TELEPORT_TEAM, new LFextra(1, 60.0)";
            case 100:
                return "LFextra.Kind.LANDING, new LFextra(-1, 94)";
            case 501:
                return "LFextra.Kind.TRANSFORM_BACK, LFextra.oneTime()";
            default:
                return null;
        }
    }
    
    public static String parserInvisibility(int invisibility) {
        return String.format("LFextra.Kind.INVISIBLE, new LFextra(%d)", invisibility);
    }
    
}

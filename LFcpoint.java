
final class LFcpoint {
    public static final LFcpoint dummy = new LFcpoint();
    public static final int _FHACT = 221;
    public static final int _BHACT = 223;
    private static final int TRANSFORM = 0b1000;
    private static final int HURTABLE  = 0b0100;
    private static final int CHANGEDIR = 0b0010;
    private static final int COVER     = 0b0001;
    
    // public final int kind;// no need
    public final int x, y, injury;
    public final int vaction;
    public final int taction;
    public final int aaction;
    public final int jaction;
    public final int throwvx;// caught's fronthurtact
    public final int throwvy;// caught's backhurtact
    public final int throwvz;
    public final int throwinjury;
    public final int dircontrol;
    public final int decrease;
    public final boolean transform;
    public final boolean changedir;
    public final boolean hurtable;
    public final boolean throwing;
    public final boolean cover;
    
    private LFcpoint() {
        x = y = injury = 0;
        vaction = taction = aaction = jaction = 0;
        throwvx = throwvy = throwvz = throwinjury = 0;
        dircontrol = decrease = 0;
        transform = changedir = throwing = cover = false;
        hurtable = true;
    }
    
    public LFcpoint(int xx, int yy, int i,
            int va, int ta, int aa, int ja,
            int tx, int ty, int tz, int ti,
            int cf, int dc, int d) {
        x = xx;
        y = yy;
        injury = i;
        vaction = va;
        taction = ta;
        aaction = aa;
        jaction = ja;
        throwvx = tx;
        throwvy = ty;
        throwvz = tz;
        throwinjury = ti;
        dircontrol = dc;
        decrease = d;
        transform = ((cf & TRANSFORM) != 0);
        hurtable = ((cf & HURTABLE) != 0);
        changedir = ((cf & CHANGEDIR) != 0);
        cover = ((cf & COVER) != 0);
        throwing = (throwvx != 0) || (throwvy != 0) || (throwvz != 0);
    }
    
    public LFcpoint(int xx, int yy, int tx, int ty) {
        this(xx, yy, 0, 0, 0, 0, 0, tx, ty, 0, 0, 0, 0, 0);
    }
    
    public static String[] parserCpoint(String hurtable, String cover, String throwinjury) {
        /* 0 b [transform] [hurtable] [changedir] [cover] */
        int injury = Integer.parseInt(throwinjury);
        int coverInt = Integer.parseInt(cover);
        char[] catchee = new char[] { '0', 'b',
            (injury == -1) ? '1' : '0',
            (Integer.parseInt(hurtable) == 0) ? '0' : '1',
            (coverInt >= 10) ? '0' : '1',
            (coverInt % 10 == 0) ? '0' : '1'
        };
        return new String[] { Integer.toString(Math.max(0, injury)), String.valueOf(catchee) };
    }
    
}

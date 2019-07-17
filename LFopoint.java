import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

final class LFopoint {
    /* guarantee the swawn object will be rendered above the creator (e.g., explosion) */
    public static final double ZOFFSET = 0.001;
    /* (self-test value) initial z-velocity for those quantity more than 1 */
    public static final double vzRange = 5.0;
    public static final double vzMultiplier = 2.5;
    public static final int NORM = 1;
    public static final int HOLD = 2;
    
    public final int kind;
    public final int x, y;
    public final int action;
    public final int dvx;
    public final int dvy;
    public final String oid;
    public final int facing;
    public final double hp;
    
    public LFopoint(int k, int xx, int yy, int dx, int dy, String i, int a, int f) {
        this(k, xx, yy, dx, dy, i, a, f, 500.0);
    }
    
    public LFopoint(int k, int xx, int yy, int dx, int dy, String i, int a, int f, double h) {
        kind = k;
        x = xx;
        y = yy;
        dvx = dx;
        dvy = dy;
        oid = i;
        action = a;
        facing = f;
        hp = h;
    }
    
    public ArrayList<LFobject> launch(final LFobject o, double ctrlVz) {
        final int quantity = Math.max(facing / 10, 1);
        ArrayList<LFobject> output = new ArrayList<>(quantity);
        final LFobject target = idmapWrapper(oid);
        if (target == null)
            return output;
        /* some of states cannot have z-velocity */
        
        double zStep = (quantity == 1) ? 0.0 : (2.0 * vzRange / (quantity - 1));
        boolean sameFacing = (facing & 1) == 0;
        for (int i = 0; i < quantity; ++i) {
            LFobject v = null;
            double thisVz = (quantity == 1) ? 0.0 : (i * zStep - vzRange);
            double thisVx = (quantity == 1) ? dvx : Math.copySign(Math.sqrt(dvx * dvx - thisVz * thisVz * vzMultiplier), dvx);
            if (o.faceRight) {
                if (sameFacing) {
                    v = target.makeCopy( true, o.teamID);
                    v.vx =  thisVx;
                } else {
                    v = target.makeCopy(false, o.teamID);
                    v.vx = -thisVx;
                }
                v.initialization(o.px - o.currFrame.centerR + x, o.py - o.currFrame.centerY + y, o.pz + ZOFFSET, action);
            } else {
                if (sameFacing) {
                    v = target.makeCopy(false, o.teamID);
                    v.vx = -thisVx;
                } else {
                    v = target.makeCopy( true, o.teamID);
                    v.vx =  thisVx;
                }
                v.initialization(o.px + o.currFrame.centerR - x, o.py - o.currFrame.centerY + y, o.pz + ZOFFSET, action);
            }
            v.vy = dvy;
            v.vz = v.currFrame.state.createVz ? (ctrlVz + thisVz) : thisVz;
            output.add(v);
        }
        if (quantity > 1 && (target instanceof LFweapon)) {
            /* e.g., Rudolf's 5 shurikens */
            for (LFobject x: output) {
                for (LFobject y: output)
                    x.immune.add(y);
            }
        }
        if (kind == HOLD && (target instanceof LFweapon) && (o instanceof LFhero)) {
            if (((LFhero)o).weapon != LFweapon.dummy)
                ((LFhero)o).weapon.picker = null;
            /* randomly choose one of the spawn objects as weapon */
            LFweapon w = (LFweapon)output.get(ThreadLocalRandom.current().nextInt(output.size()));
            (w.picker = (LFhero)o).weapon = w;
        }
        return output;
    }
    
    public static LFobject idmapWrapper(String oid) {
        final LFobject target = LFX.objPool.getOrDefault(oid, null);
        if (target == null)
            System.err.printf("Error: Required LFobject `%s' is not existing.\n", oid);
        return target;
    }
    
    public static ArrayList<LFobject> createArmour(final LFobject o) {
        final ThreadLocalRandom rc = ThreadLocalRandom.current();
        /* implementation is very likely different from LF2 */
        ArrayList<LFobject> output = new ArrayList<>(5);
        LFobject amrour1 = idmapWrapper("Louisarmour1");
        if (amrour1 != null) {
            final double[] dirX = { 1.0, 1.0, -1.0, -1.0 };
            final double[] dirZ = { 1.0, -1.0, 1.0, -1.0 };
            for (int i = 0; i < 4; ++i) {
                LFobject v = amrour1.makeCopy(i > 1, o.teamID);
                v.vx = (rc.nextDouble(9.0) + 6.0) * dirX[i];
                v.vz = (rc.nextDouble(4.0) + 3.0) * dirZ[i];
                v.vy = (rc.nextDouble(3.0) - 8.0);
                v.initialization(o.px, o.py - o.currFrame.centerY / 2.0, o.pz, 0);
                output.add(v);
            }
        }
        LFobject amrour2 = idmapWrapper("Louisarmour2");
        if (amrour2 != null) {
            LFobject v = amrour2.makeCopy(rc.nextBoolean(), o.teamID);
            v.vx = rc.nextDouble(9.6) - 4.8;
            v.vz = rc.nextDouble(6.0) - 3.0;
            v.vy = rc.nextDouble(2.5) - 6.0;
            v.initialization(o.px, o.py - o.currFrame.centerY / 2.0, o.pz, 0);
            output.add(v);
        }
        /* apparently they immune to each other while in the sky */
        for (LFobject x: output) {
            for (LFobject y: output)
                x.immune.add(y);
        }
        return output;
    }
    
    public static String parserType(String originalKind) {
        switch (originalKind) {
            case "1":
                return "LFopoint.NORM";
            case "2":
                return "LFopoint.HOLD";
            default:
                return null;
        }
    }
    
}

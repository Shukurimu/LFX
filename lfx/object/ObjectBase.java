package lfx.object
			x1 = px - owner.currFrame.centerR + itr.x;
			x2 = x1 + itr.w;
		} else {
			x2 = px + owner.currFrame.centerR - itr.x;
			x1 = x2 - itr.w;
		}
		y1 = owner.py - owner.currFrame.centerY + itr.y;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.WeakHashMap;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import lfx.component.Bdy
import lfx.component.Itr
/** https://github.com/Project-F/F.LF/tree/master/LF
    https://www.lf-empire.de/forum/showthread.php?tid=10733
    https://lf-empire.de/lf2-empire/data-changing/types/167-effect-0-characters
    https://lf-empire.de/lf2-empire/data-changing/reference-pages/182-states?showall=1&limitstart=
    http://lf2.wikia.com/wiki/Health_and_mana
    http://gjp4860sev.myweb.hinet.net/lf2/page10.htm */

abstract class ObjectBase implements Cloneable {
    public static final int DEFAULT_ACT = 1236987450;  // arbitrary number
    private static int teamIDcounter = 32;
    protected static final int Act_NX0 = 0;
    protected static final int Act_1K = 1000;
    protected static final int Act_999 = 999;
    protected static final int NOP = 0;
    protected static final int HITLAG_SPAN = 3;
    protected static final int[] WHOLE_MAP = { -1048576, -1048576, 2097152, 2097152, 65536 };
    
    /* wrap images in a JavaFX Node */
    private ImageView fxNode = null;
    /* fields with `final' modifier are shared in objects with same kind */
    private final LFframe[] frame;
    public final LFtype type;
    public final String identifier;
    public final ArrayList<Image> picture1;
    public final ArrayList<Image> picture2;
    public ArrayList<LFbdyarea> currBdy = null;
    public ArrayList<LFitrarea> currItr = null;
    public ArrayList<LFitrarea> recvItr = null;
    public WeakHashMap<LFobject, Integer> vrest = null;
    public EnumMap<LFextra.Kind, LFextra> extra = null;
    public HashSet<LFobject> immune = null;
    public LFdamage recvDmg = null;
    public int arest = 0;
    public LFframe currFrame = null;
    public boolean faceRight = true;
    public double px = 0.0, py = 0.0, pz = 0.0;
    public double vx = 0.0, vy = 0.0, vz = 0.0;
    protected double hp = 500.0, hpMax = 500.0;
    protected double mp = 200.0, mpMax = 500.0;
    public int waitTU = 0;
    public int hitLag = 0;
    public int teamID = 0;
    protected LFobject origin = null;
    protected LFobject transf = null;
    public LFgrasp grasp = null;
    
    protected LFobject(String id, LFtype t, int frameCount) {
        identifier = id;
        type = t;
        frame = new LFframe[frameCount];
        picture1 = new ArrayList<>(210);
        picture2 = new ArrayList<>(210);
    }
    
    /* called by subclasses' constructor */
    protected final LFframe.Builder setFrame(int frameIndex, int pictureIndex, LFstate s,
                                    int w, int n, int dx, int dy, int dz, int cx, int cy) {
        if (frame[frameIndex] != null)
            System.out.printf("%s: Frame%d is overwritten\n", identifier, frameIndex);
        pictureIndex = (pictureIndex < picture1.size()) ? pictureIndex : 0;
        Image image1 = picture1.get(pictureIndex);
        Image image2 = picture2.get(pictureIndex);
        return new LFframe.Builder(frame, frameIndex, image1, image2, s, w, n, dx, dy, dz, cx, cy);
    }
    
    /* for LFmap accessing */
    public final ImageView getNode() {
        return fxNode;
    }
    
    /* return current up and down keypress value (e.g., throw object, launch blast) */
    public double getControlZ() {
        return 0.0;
    }
    
    public final void initialization(double px, double py, double pz, int actionNumber) {
        currFrame = frame[Math.abs(actionNumber)];
        waitTU = currFrame.wait;
        this.px = px;
        this.py = py;
        this.pz = pz;
        return;
    }
    
    public final void applyExtra() {
        if (waitTU == currFrame.wait)
            currFrame.ext.forEach((k, e) -> extra.compute(k, e::stack));
        return;
    }
    
    /* return the corresponding scope view from o's perspective */
    public abstract int checkItrScope(LFobject o);
    
    /* this.itrs o.bdys  */
    public final void checkItr(final LFobject o, final LFmap map) {
        if (o == this || arest > map.mapTime || vrest.getOrDefault(o, 0) > map.mapTime || immune.contains(o))
            return;
        
        final int bdyScope = o.checkItrScope(this);
        /* since there is only one itr can take effect in one frame
           you should break this loop once you find a pair of itr and bdy */
        CHECK_ITR_LOOP:
        for (LFitrarea ia: currItr) {
            for (LFbdyarea ba: o.currBdy) {
                if (!ba.collide(ia))
                    continue;
                if ((ba.bdy).isImmuneTo(ia.itr, bdyScope))
                    continue;
                if (ia.itr.effect.normalEffect) {
                    damageCallback(ia.itr, o);
                    o.damageReceived(ia, ba);
                    if (ia.itr.vrest > 0)
                        vrest.put(o, map.mapTime + ia.itr.vrest);
                    else
                        arest = map.mapTime - ia.itr.vrest;
                    break CHECK_ITR_LOOP;
                } else switch (ia.itr.effect) {
                    case REFLECT:
                        damageCallback(ia.itr, o);
                        o.damageReceived(ia, ba);
                        if (o instanceof LFhero)
                            hp = 0.0;
                        break CHECK_ITR_LOOP;
                    case HEAL:
                        o.recvItr.add(ia);
                        setCurr(ia.itr.dvx);
                        break CHECK_ITR_LOOP;
                    case VORTEX:
                    case SONATA:
                    case LETSP:
                    case FENCE:
                        o.recvItr.add(ia);
                        break CHECK_ITR_LOOP;
                    case GRASPDOP:
                        if (o.currFrame.state != LFstate.DOP)
                            break;
                    case GRASPBDY:
                        if (LFgrasp.initialize(this, ia.itr.dvx, o, ia.itr.dvy))
                            break CHECK_ITR_LOOP;
                        break;
                    case PICKROLL:
                        /* you cannot pick heavy type weapons while rolling */
                        if (o.type == LFtype.HEAVY)
                            break;
                    case PICKSTAND:
                        if (o.currFrame.state != LFstate.ONGROUND)
                            break;
                        /* you can only pick a weapon belonging to nobody and you have no weapon yet */
                        if (((LFweapon)o).picker == null && ((LFhero)this).weapon == LFweapon.dummy) {
                            ((LFweapon)o).picker = (LFhero)this;
                            ((LFhero)this).weapon = (LFweapon)o;
                            recvItr.add(ia);
                        }
                        break;
                    default:
                        System.out.printf("\nUnknown ItrKind: %s", ia.itr.effect);
                }
            }
        }
        return;
    }
    
    protected abstract int resolveAct(int index);
    
    /* should only be called while dealing with itrs or in the process of transit to next frame */
    public void setCurr(int index) {
        faceRight ^= (index < 0);
        currFrame = frame[resolveAct(index)];
        waitTU = currFrame.wait;
        return;
    }
    
    public void setNext(LFframe nextFrame) {
        faceRight ^= (currFrame.next < 0);
        currFrame = nextFrame;
        waitTU = currFrame.wait;
        return;
    }
    
    /* return null if remove */
    protected final LFframe getFrame(int index) {
        return (index == Act_1K) ? null : frame[(index == Act_NX0) ? currFrame.curr : resolveAct(index)];
    }
    
    /* invoked when F7 is pressed */
    public abstract void revive();
    
    /* invoked when itrs interact with bdys */
    public abstract void damageReceived(LFitrarea ia, LFbdyarea ba);
    public abstract void damageCallback(LFitr i, LFobject o);
    
    /* these methods are invoked when action changed */
    protected abstract void registerItr();
    protected abstract void registerBdy();
    
    /* these methods are invoked every TimeUnit */
    protected abstract boolean reactAndMove(LFmap map);
    protected abstract boolean checkBoundary(LFmap map);
    
    private boolean objectExist = true;
    /* called by LFmap, return false if this object is no longer used in current map */
    public final void updateStatus(LFmap map) {
        objectExist = reactAndMove(map) && checkBoundary(map);
        return;
    }
    
    public final boolean updateFXnode() {
        if (objectExist) {
            fxNode.setX(px - (faceRight ? currFrame.centerR : currFrame.centerL));
            fxNode.setY(pz + py - currFrame.centerY);
            fxNode.setViewOrder(pz);
            fxNode.setImage(faceRight ? currFrame.image1 : currFrame.image2);
            return true;
        } else {
            /* for JavaFx ObservableList */
            fxNode.setVisible(false);
            fxNode = null;
            return false;
        }
    }
    
    /* do transform */
    public void statusOverwrite(final LFhero target) {
        target.vrest = vrest;
        target.extra = extra;
        target.arest = arest;
        target.hpMax = hpMax;
        target.mpMax = mpMax;
        target.hp = hp;
        target.mp = mp;
        /* self cleaning */
        hitLag = 0;
        vx = vy = vz = 0.0;
        recvDmg.reset();
        immune.clear();
        if (grasp != null) {
            grasp.state = LFgrasp.State.TIMEUP;
            grasp  = null;
        }
        return;
    }
    
    /* used in FrameBuilder to set affecting area */
    @SafeVarargs
    protected static final int[] xywhz(int... args) {
        if (args.length == 4)
            return new int[] { args[0], args[1], args[2], args[3], LFbdy._Z };
        if (args.length == 5)
            return new int[] { args[0], args[1], args[2], args[3], args[4] };
        System.err.println("xywhz: arguments length should be 4 or 5");
        return new int[] { 0, 0, 0, 0, 0 };
    }
    
    public static Image loadImage(String path) {
        Image src = null;
        try {
            src = new Image(LFobject.class.getResource(path).openStream());
        } catch (Exception ouch) {
            ouch.printStackTrace();
            System.err.println("Exception in loading " + path);
        }
        return src;
    }
    
    /* some Bitmap are not loaded correctly in JavaFX, you should pre-convert it to standard format */
    protected final void loadImageCells(String path, int w, int h, int row, int col) {
        Image origin = loadImage(path);
        final int ow = (int)origin.getWidth();
        final int oh = (int)origin.getHeight();
        final int ln = ow * oh;
        int[] pixels = new int[ln];
        /* row values might be out of bound */
        int realRow = Math.min(row, (ow + 1) / (w + 1));
        
        if (origin.isError()) {
            // (new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                // "Resource Error: " + path + "\n" + origin.getException())).showAndWait();
            System.out.println("Resource Error: " + path + "\n" + origin.getException());
            javafx.application.Platform.exit();
            return;
        }
        PixelReader reader = origin.getPixelReader();
        reader.getPixels(0, 0, ow, oh, PixelFormat.getIntArgbPreInstance(), pixels, 0, ow);
        for (int i = 0; i < ln; ++i) {
            if (pixels[i] == 0xff000000)
                pixels[i] = 0;
        }
        WritableImage buff1 = new WritableImage(ow, oh);
        buff1.getPixelWriter().setPixels(0, 0, ow, oh, PixelFormat.getIntArgbPreInstance(), pixels, 0, ow);
        
        /* programmically mirror images */
        for (int r = 0; r < oh; ++r) {
            if ((r + 1) % (h + 1) == 0)
                continue;
            final int s = r * ow;
            for (int c = 0; c < realRow; ++c) {
                final int x = s + c * (w + 1);
                for (int i = x, j = x + w - 1; i < j; ++i, --j) {
                    int ptemp = pixels[i];
                    pixels[i] = pixels[j];
                    pixels[j] = ptemp;
                }
            }
        }
        WritableImage buff2 = new WritableImage(ow, oh);
        buff2.getPixelWriter().setPixels(0, 0, ow, oh, PixelFormat.getIntArgbPreInstance(), pixels, 0, ow);
        /* index out of bound showing nothing */
        WritableImage nothing = new WritableImage(w, h);
        
        PixelReader reader1 = buff1.getPixelReader();
        PixelReader reader2 = buff2.getPixelReader();
        for (int i = 0; i < col; ++i) {
            for (int j = 0; j < row; ++j) {
                /* plus one for 1px seperating line */
                final int x = j * (w + 1);
                final int y = i * (h + 1);
                if (x + w < ow && y + h < oh) {
                    picture1.add(new WritableImage(reader1, x, y, w, h));
                    picture2.add(new WritableImage(reader2, x, y, w, h));
                } else {
                    picture1.add(nothing);
                    picture2.add(nothing);
                }
            }
        }
        return;
    }
    
    /* invoked in the end of constructor */
    protected void preprocess() {
        picture1.trimToSize();
        picture2.trimToSize();
        return;
    }
    
    @Override
    protected LFobject clone() {
        LFobject X = null;
        try {
            X = (LFobject)super.clone();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        X.fxNode = new ImageView();
        X.currBdy = new ArrayList<>(4);
        X.currItr = new ArrayList<>(4);
        X.recvItr = new ArrayList<>(8);
        X.extra = new EnumMap<>(LFextra.Kind.class);
        X.vrest = new WeakHashMap<>();
        X.immune = new HashSet<>();
        X.recvDmg = new LFdamage();
        X.origin = X;
        return X;
    }
    
    /* use clone object to save memory */
    public final LFobject makeCopy(boolean f, int t) {
        System.out.print("[LFobject.clone]");
        LFobject  X = (LFobject)this.clone();
        X.faceRight = f;
        X.teamID = (t != 0) ? t : ++teamIDcounter;
        return X;
    }
    
    @Override
    public final String toString() {
        return String.format("%s#%d", identifier, this.hashCode());
    }
    
}

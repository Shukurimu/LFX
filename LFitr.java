final class LFitr {
	public static final int _Z = 12;
	public static final int _DVX = 0;
	public static final int _DVY = -7;
	public static final int _BDEF = 0;
	public static final int _FALL = 20;
	public static final int _INJU = 0;
	public static final int _VREST =  9;	/* weapon */
	public static final int _AREST = -7;	/* hero */
	public final int x, y, w, h, z;
	public final int scope;
	public final LFeffect effect;
	public final int fall;
	public final int injury;
	public final int bdefend;
	public final int dvx;// catchingact
	public final int dvy;// caughtact
	public final int vrest;
	
	public LFitr(LFeffect k, int xx, int yy, int ww, int hh, int zd,
				int dx, int dy, int b, int ij, int fl, int vt, int s) {
		x = xx;
		y = yy;
		w = ww;
		h = hh;
		z = zd;
		scope = s;
		effect = k;
		dvx = dx;
		dvy = dy;
		bdefend = b;
		injury = ij;
		fall = fl;
		vrest = vt;
	}
	
	public LFitr(LFeffect k, int[] xywhz,
				int dx, int dy, int b, int ij, int fl, int vt, int s) {
		this(k, xywhz[0], xywhz[1], xywhz[2], xywhz[3], xywhz[4], dx, dy, b, ij, fl, vt, s);
	}
	
	/* catch effect */
	public LFitr(LFeffect k, int[] xywhz, int c1, int c2, int s) {
		this(k, xywhz[0], xywhz[1], xywhz[2], xywhz[3], xywhz[4], c1, c2, 0, 0, 0, 0, s);
	}
	
	/* other effect */
	public LFitr(LFeffect k, int[] xywhz, int s) {
		this(k, xywhz[0], xywhz[1], xywhz[2], xywhz[3], xywhz[4], 0, 0, 0, 0, 0, 0, s);
	}
	
	/* weapon strength list */
	public LFitr(LFeffect k, int dx, int dy, int b, int ij, int fl, int vt, int s) {
		this(k, 0, 0, 0, 0, 0, dx, dy, b, ij, fl, vt, s);
	}
	
	/* weapon strength list to LFarea */
	public LFitr(LFitr itrField, LFitr strength) {
		this(strength.effect, itrField.x, itrField.y, itrField.w, itrField.h, itrField.z,
			strength.dvx, strength.dvy, strength.bdefend, strength.injury, strength.fall,
			strength.vrest, strength.scope
		);
	}
	
}

class LFitrarea {
	public final LFobject owner;
	public final LFitr itr;
	public final double px, pz;
	public final double x1, x2;
	public final double y1, y2;
	public final double z1, z2;
	public final boolean faceRight;
	
	public LFitrarea(LFobject o, LFitr i) {
		owner = o;
		itr = i;
		faceRight = owner.faceRight;
		px = owner.px;
		if (faceRight) {
			x1 = px - owner.currFrame.centerR + itr.x;
			x2 = x1 + itr.w;
		} else {
			x2 = px + owner.currFrame.centerR - itr.x;
			x1 = x2 - itr.w;
		}
		y1 = owner.py - owner.currFrame.centerY + itr.y;
		y2 = y1 + itr.h;
		pz = owner.pz;
		z1 = pz - itr.z;
		z2 = pz + itr.z;
	}
	
	/* `s' for the wpoint area
	   `g' for attacking strength */
	public LFitrarea(LFobject o, LFitr s, LFitr g) {
		this(o, new LFitr(s, g));
	}
	
	/* explosion-effect negative dvx goes two directions */
	public double calcDvx(LFbdyarea ba) {
		if (itr.effect.explosionType) {
			return (px < ba.px) ? (-itr.dvx) : itr.dvx;
		} else {
			return faceRight ? itr.dvx : (-itr.dvx);
		}
	}
	
	/* The followings are the self implementation of several itr kinds
	   since no documentation discussing about this effect
	   the result is very likely different from LF2 */
	public static final double SONATA_VELOCITY_DEDUCTION = 0.7;
	public static final double SONATA_Y_RATIO = 0.7;
	
	public double sonataVxz(double v) {
		return v * SONATA_VELOCITY_DEDUCTION;
	}
	
	public double sonataVy(double py, double vy) {
		return ((vy > itr.dvy) && ((y1 - y2) * SONATA_Y_RATIO + y2 < py)) ? Math.max(itr.dvy, vy + itr.dvy) : vy;
	}
	
	public static final double VORTEX_DISTANCE_MULTIPLIER = 0.12;
	
	public double vortexAx(double x) {
		final double length = x2 - x1;
		return Math.sin(Math.PI * (px - x) / length) * Math.sqrt(length) * VORTEX_DISTANCE_MULTIPLIER;
	}
	
	public double vortexAz(double z) {
		final double length = z2 - z1;
		return Math.sin(Math.PI * (pz - z) / length) * Math.sqrt(length) * VORTEX_DISTANCE_MULTIPLIER;
	}
	
	public double vortexAy(double y, double vy) {
		return (LFX.currMap.gravity * ((y1 - y) / (y2 - y1) - 1.0)) - ((vy > 0.0) ? (vy * 0.16) : 0.0);
	}
	
}

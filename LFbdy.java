
final class LFbdy {
	public static final int _Z = 12;
	public final int x, y, w, h, z;
	public final int scope;
	/* used in LFitr T-teammate E-enemy */
	public static final int  HEROt = 0b000001;
	public static final int  HEROe = 0b000010;
	public static final int  WEAPt = 0b000100;
	public static final int  WEAPe = 0b001000;
	public static final int BLASTt = 0b010000;
	public static final int BLASTe = 0b100000;
	public static final int ALLOBJ = 0b111111;
	/* used in LFbdy */
	public static final int ALLDMG = 0b00001;// friendly fire
	public static final int  FIRE2 = 0b00010;// immune to FIRE2
	public static final int   ICE2 = 0b00100;// immune to ICE2
	public static final int   FALL = 0b01000;// immune to FALL less than 41
	public static final int DEFICE = ALLDMG | ICE2;
	
	public LFbdy(int xx, int yy, int ww, int hh, int zd, int s) {
		x = xx;
		y = yy;
		w = ww;
		h = hh;
		z = zd;
		scope = s;
	}
	
	/* basic */
	public LFbdy(int xx, int yy, int ww, int hh) {
		this(xx, yy, ww, hh, _Z, 0);
	}
	
	/* immune to specified itr effect */
	public LFbdy(int xx, int yy, int ww, int hh, int i) {
		this(xx, yy, ww, hh, _Z, i);
	}
	
	public boolean isImmuneTo(LFitr i, int bdyScope) {
		return ((((scope &  FIRE2) != 0) && (i.effect == LFeffect.FIRE2)) ||
				(((scope &   ICE2) != 0) && (i.effect == LFeffect.ICE2)) ||
				(((scope &   FALL) != 0) && (i.fall < 41)) ||
			  (((((scope & ALLDMG) != 0) ? (i.scope | (i.scope >> 1)) : i.scope) & bdyScope) == 0));
	}
	
	public static String parserBdy(int originalState, String identifier) {
		if (identifier.equals("Freezecolumn"))
			return ", LFbdy.ALLDMG";
		switch (originalState) {
			case 13:
				return ", LFbdy.DEFICE";
			case 18:
			case 19:
				return ", LFbdy.FIRE2";
			case 12:
				return ", LFbdy.FALL";
			default:
				return "";
		}
	}
	
}

final class LFbdyarea {
	public final LFobject owner;
	public final LFbdy bdy;
	public final double px, pz;
	public final double x1, x2;
	public final double y1, y2;
	public final double z1, z2;
	public final boolean faceRight;
	
	public LFbdyarea(LFobject o, LFbdy b) {
		owner = o;
		bdy = b;
		faceRight = owner.faceRight;
		px = owner.px;
		if (faceRight) {
			x1 = px - owner.currFrame.centerR + bdy.x;
			x2 = x1 + bdy.w;
		} else {
			x2 = px + owner.currFrame.centerR - bdy.x;
			x1 = x2 - bdy.w;
		}
		y1 = owner.py - owner.currFrame.centerY + bdy.y;
		y2 = y1 + bdy.h;
		pz = owner.pz;
		z1 = pz - bdy.z;
		z2 = pz + bdy.z;
	}
	
	/* check collision and restriction */
	public boolean collide(LFitrarea ia) {
		return ((x1 < ia.x2) && (x2 > ia.x1) &&
				(z1 < ia.z2) && (z2 > ia.z1) &&
				(y1 < ia.y2) && (y2 > ia.y1));
	}
	
}

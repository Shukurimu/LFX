import javafx.scene.image.Image;

abstract class LFblast extends LFobject {
    /* https://www.lf-empire.de/en/lf2-empire/data-changing/reference-pages/183-effect-3-hitfa */
    public static final double CHASE_AX = 0.7;
    public static final double CHASE_VXMAX = 14.0;
    public static final double CHASE_VXOUT = 17.0;
    public static final double CHASE_VY = 1.0;
    public static final double CHASE_AZ = 0.4;
    public static final double CHASE_VZMAX = 2.2;
    public static final int DESTROY_TIME = 16;
    public static final int REFLECT_VREST = 8;/* see below */
    
    protected String soundHit;
    protected String soundDrop;
    protected String soundBroken;
    /* see function reactAndMove return conditions  */
    private int destroyCountdown = DESTROY_TIME;
    private LFobject focusOn = null;
    private LFobject reflector = null;

    protected LFblast(String id, LFtype t) {
        super(id, t, 400);
        picture1.ensureCapacity(220);
        picture2.ensureCapacity(220);
    }
    
    public final void setState(String sh, String sd, String sb) {
        soundHit = sh;
        soundDrop = sd;
        soundBroken = sb;
        return;
    }
    
    @Override
    public final int checkItrScope(LFobject o) {
        /* you can rebounce a friendly blase only if you are not a blast and facing to different direction */
        return (o.teamID == teamID) ? (((o instanceof LFblast) || (faceRight == o.faceRight)) ? 0b010000 : 0b110000) : 0b100000;
    }
    
    @Override
    public final void revive() {
        hp = hpMax;
        return;
    }
    
    @Override
    public final void damageCallback(LFitr i, LFobject o) {
        if (currFrame.state == LFstate.ENERGY) {
            if (o.currFrame.state == LFstate.ENERGY) {
                setCurr(Act_hitfail);
                vx = vy = vz = 0.0;
                return;
            }
        } else if (currFrame.state == LFstate.PIERCE) {
            if (o.currFrame.state == LFstate.ENERGY || o.currFrame.state == LFstate.PIERCE) {
                setCurr(Act_hitfail);
                vx = vy = vz = 0.0;
                return;
            }
        } else if (currFrame.state == LFstate.NORMAL) {
            setCurr((o instanceof LFblast) ? Act_hitfail : Act_hitsucc);
            vx = vy = vz = 0.0;
            return;
        }
        hitLag = HITLAG_SPAN;
        return;
    }
    
    @Override
    public void damageReceived(LFitrarea ia, LFbdyarea ba) {
        if (ia.itr.effect == LFeffect.REFLECT) {
            reflector = ia.owner;
            if (currFrame.state == LFstate.ENERGY)
                setCurr(Act_disappear);
            else {
                teamID = ia.owner.teamID;
                setCurr(Act_rebound);
            }
            vx = vy = vz = 0.0;
            return;
        } else if (currFrame.state == LFstate.ENERGY) {
            if (ia.owner.currFrame.state == LFstate.ENERGY) {
                setCurr(Act_hitfail);
                vx = vy = vz = 0.0;
                return;
            }
        } else if (currFrame.state == LFstate.PIERCE) {
            if (ia.owner.currFrame.state == LFstate.ENERGY) {
                teamID = ia.owner.teamID;
                setCurr((ia.owner instanceof LFhero) ? Act_rebound : Act_hitfail);
                vx = vy = vz = 0.0;
                return;
            }
            if (ia.owner.currFrame.state == LFstate.PIERCE) {
                setCurr(Act_hitfail);
                vx = vy = vz = 0.0;
                return;
            }
        } else if (currFrame.state == LFstate.NORMAL) {
            if (ia.owner instanceof LFhero) {
                teamID = ia.owner.teamID;
                setCurr(Act_rebound);
            } else
                setCurr(Act_hitfail);
            vx = vy = vz = 0.0;
            return;
        }
        hitLag = HITLAG_SPAN;
        return;
    }
    
    @Override
    public final void setCurr(int index) {
        super.setCurr(index);
        registerItr();
        registerBdy();
        return;
    }
    
    @Override
    protected final int resolveAct(int index) {
        if (index < 0)
            index = -index;
        return (index == Act_999) ? 0 : index;
    }
    
    protected static final int Act_flying = 0;
    protected static final int Act_hitsucc = 10;
    protected static final int Act_hitfail = 20;
    protected static final int Act_rebound = 30;
    protected static final int Act_disappear = 40;
    protected static final int HitFa_dennischase = 2;
    protected static final int HitFa_johndiskchase = 1;
    protected static final int HitFa_johndisk2 = 10;
    
    
    
    
    @Override
    public boolean reactAndMove(LFmap map) {
        for (LFitrarea r: recvItr) {
            switch (r.itr.effect) {
                case FENCE:
                    extra.put(LFextra.Kind.MOVEBLOCK, LFextra.oneTime());
                    break;
                case REFLECT:
                    System.out.printf("\nImplementing ItrKind: %s", r.itr.effect);
                    break;
                default:
                    System.out.printf("\n%s should not receive ItrKind: %s", this, r.itr.effect);
            }
        }
        recvItr.clear();
        
        int nextAct = DEFAULT_ACT;
        /* I found that the reflect itr causes target and its direct spawned objects
           immune to the itr owner for about 8 TimeUnit */
        if (reflector != null && currFrame.state != LFstate.REBOUND)
            reflector  = null;
        /* opoint is triggered only at the first timeunit */
        if (waitTU == currFrame.wait && !currFrame.opoint.isEmpty()) {
            for (LFopoint x: currFrame.opoint)
                map.spawnObject(x.launch(this, 0.0), reflector, REFLECT_VREST);
        }
        
        if (currFrame.comboList[LFact.hit_a.index] != 0) {
            if ((hp -= currFrame.comboList[LFact.hit_a.index]) > 0.0) {
                if (currFrame.comboList[LFact.hit_Fa.index] != 0) {
                    /* randomly choose an alive enemy */
                    if (focusOn == null || focusOn.hp == 0.0)
                        focusOn = map.chooseHero(this, false, null);
                    if (focusOn != null) {
                        switch (currFrame.comboList[LFact.hit_Fa.index]) {
                            case HitFa_johndiskchase:
                            case HitFa_dennischase:
                                if (((focusOn.px - px) >= 0.0) == (vx >= 0.0)) {
                                    /* straight34 */
                                    if (currFrame.curr != 3 && currFrame.curr != 4)
                                        nextAct = 3;
                                } else {
                                    /* changedir12 */
                                    if (currFrame.curr == 3 || currFrame.curr == 4)
                                        nextAct = 1;
                                }
                                py += Math.copySign(CHASE_VY, focusOn.py - focusOn.currFrame.centerY / 2.0 - py);
                                vx = (focusOn.px >= px) ?
                                    Math.min(CHASE_VXMAX, vx + CHASE_AX) : Math.max(-CHASE_VXMAX, vx - CHASE_AX);
                                vz = (focusOn.pz >= pz) ?
                                    Math.min(CHASE_VZMAX, vz + CHASE_AZ) : Math.max(-CHASE_VZMAX, vz - CHASE_AZ);
                                faceRight = (vx >= 0.0);
                                break;
                            case HitFa_johndisk2:
                                vx = (vx >= 0.0) ? CHASE_VXOUT : -CHASE_VXOUT;
                                vz = 0.0;
                                break;
                        }
                    }
                }
            } else {
                switch (currFrame.comboList[LFact.hit_Fa.index]) {
                    case HitFa_dennischase:
                        if (currFrame.curr != 5 && currFrame.curr != 6)
                            nextAct = 5;
                        vx = (vx >= 0.0) ? Math.min(CHASE_VXOUT, vx + CHASE_AX) : Math.max(-CHASE_VXOUT, vx - CHASE_AX);
                        vy = 0.0;
                        break;
                    case HitFa_johndisk2:
                        vx = (vx >= 0.0) ? CHASE_VXOUT : -CHASE_VXOUT;
                        vz = 0.0;
                        break;
                    default:
                        if (currFrame.comboList[LFact.hit_d.index] != 0) {
                            if (currFrame.comboList[LFact.hit_d.index] > 0) {
                                nextAct =  currFrame.comboList[LFact.hit_d.index];
                            } else {
                                faceRight = !faceRight;
                                nextAct = -currFrame.comboList[LFact.hit_d.index];
                            }
                        }
                }
            }
        }
        
        /* block all velocity and position changes if the chatacter in hitlag duration */
        if (hitLag == 0) {
            vx = currFrame.calcVX(vx, faceRight);
            px = extra.containsKey(LFextra.Kind.MOVEBLOCK) ? px : (px + vx);
            vy = currFrame.calcVY(vy);
            if (extra.containsKey(LFextra.Kind.MOVEBLOCK) || (currFrame.dvz == LFframe.DV_550))
                vz = 0.0;
            else
                pz += vz + currFrame.comboList[LFact.hit_j.index];
        }
        
        /* process next frame */
        if (hitLag > 0) {
            --hitLag;
        } else if (nextAct != DEFAULT_ACT) {
            setCurr(nextAct);
        } else if (--waitTU < 0) {
            LFframe nextFrame = getFrame(currFrame.next);
            if (nextFrame == null)
                return false;
            setNext(nextFrame);
        }
        
        registerItr();
        registerBdy();
        return true;
    }
    
    @Override
    protected boolean checkBoundary(LFmap map) {
        
        if (px > map.xwidthl && px < map.xwidthr) {
            /* refresh countdown timer if in map bound */
            destroyCountdown = DESTROY_TIME;
        } else if ((currFrame.comboList[LFact.hit_Fa.index] == 0 || hp < 0.0) && (--destroyCountdown < 0)) {
            /* even the blast flies out of bound and is not a functional frame (hit_Fa)
               it still has a short time to live (e.g., dennis chase first 4 frames) */
            return false;
        }
        pz = (pz > map.zboundB) ? map.zboundB : ((pz < map.zboundT) ? map.zboundT : pz);
        return true;
    }
    
    @Override
    public void registerItr() {
        currItr.clear();
        for (LFitr i: currFrame.itr)
            currItr.add(new LFitrarea(this, i));
        return;
    }
    
    @Override
    public void registerBdy() {
        currBdy.clear();
            for (LFbdy b: currFrame.bdy)
                currBdy.add(new LFbdyarea(this, b));
        return;
    }
    
    @Override
    public void statusOverwrite(final LFhero target) {
        super.statusOverwrite(target);
        target.hp2nd = hp;
        return;
    }
    
    @Override
    protected final LFblast clone() {
        System.out.printf("%s.clone()\n", identifier);
        return (LFblast)super.clone();
    }
    
}

package lfx.component;

public class LFgrasp {
    enum State {
        NOGRASP,
        CATCHING,
        THROW_START,
        THROW_END,
        DROP,// catchee act to jump
        TIMEUP;// catchee fall down
    }
    public static final LFgrasp dummy = new LFgrasp();
    public static final int TOTAL_GRASP_TIME = 305;/* self-test value */
    public LFcpoint cpoint = null;
    public State state = State.CATCHING;
    private int timeLeft = Integer.MAX_VALUE;/* initial state */
    private int currFrameWaitTU = 0;
    private int lastFrameNumber = 0;
    private final LFobject catcher;
    private final LFhero catchee;
    private boolean catcherUpdated = false;
    /* these flags are set by catcher and used by catchee */
    private int injury = 0;

    private LFgrasp() {
        state = State.NOGRASP;
        catcher = null;
        catchee = null;
    }

    private LFgrasp(LFobject o1, LFhero o2) {
        catcher = o1;
        catcher.grasp = this;
        catchee = o2;
        catchee.grasp = this;
        catchee.fp = 0;/* reset fall point */
    }

    public synchronized static boolean initialize(LFobject o1, int catchingact, LFobject o2, int caughtact) {
        /* only LFhero can be caught */
        if (!(o2 instanceof LFhero))
            return false;
        o1.setCurr(catchingact);
        o2.setCurr(caughtact);
        new LFgrasp(o1, (LFhero)o2);
        return true;
    }

    public boolean isNotCaught(LFobject h) {
        return (state != State.CATCHING) || (catcher == h);
    }

    public boolean hasBdy(LFobject h) {
        return (state != State.CATCHING) || ((catchee == h) && cpoint.hurtable);
    }

    public void update(LFobject o) {
        if (o == catcher)
            catcherUpdate();
        else
            catcheeUpdate();
        return;
    }

    private synchronized void catcherUpdate() {
        cpoint = catcher.currFrame.cpoint;
        if (state != State.CATCHING || cpoint == null) {
            /* null case: catcher does combo and jumps to a frame without cpoint */
            state  = State.DROP;
        } else {
            /* no time left in the first timeunit */
            timeLeft = (timeLeft == Integer.MAX_VALUE) ? TOTAL_GRASP_TIME : (timeLeft - Math.abs(cpoint.decrease));
            /* catchee falls down only in the frame with negative cpoint.decrease */
            if (timeLeft < 0 && cpoint.decrease < 0) {
                state = State.TIMEUP;
            } else {
                /* only do in the first TimeUnit (assume frame number < 1024) */
                if ((lastFrameNumber != catcher.currFrame.curr) || (currFrameWaitTU == 0)) {
                    lastFrameNumber = catcher.currFrame.curr;
                    if (((injury = cpoint.injury) != 0) && (injury > 0)) {
                        catcher.hitLag = LFobject.HITLAG_SPAN;
                        currFrameWaitTU += LFobject.HITLAG_SPAN;
                    }
                    if (cpoint.throwing)
                        state = State.THROW_START;
                    if (cpoint.transform) {
                        /* forcibly release catchee */
                        state = State.THROW_START;
                        catcher.extra.put(LFextra.Kind.TRANSFORM_TO, new LFextra(1, catchee.identifier));
                    }
                }
                currFrameWaitTU = catcher.waitTU;
            }
        }
        catcherUpdated = true;
        this.notify();
        return;
    }

    private synchronized void catcheeUpdate() {
        while (!catcherUpdated) {
            try {
                this.wait(900);
            } catch (InterruptedException ex) {}
        }

        if (state == State.DROP || state == State.TIMEUP || state == State.THROW_END)
            return;
        if (state == State.THROW_START) {
            state = State.THROW_END;
            catchee.vx = catcher.faceRight ? cpoint.throwvx : -cpoint.throwvx;
            catchee.vy = cpoint.throwvy;
            catchee.vz = catcher.getControlZ() * cpoint.throwvz;
            catchee.setCurr(cpoint.vaction);
            catchee.extra.put(LFextra.Kind.THROWINJURY, new LFextra(-1, cpoint.throwinjury));
            /* `injury' tag is useless while throwing in LF2, but you can set it in LFX */
            catchee.hpLost(injury, false);
            return;
        }
        /* e.g., hit by characters other than the catcher and remain the action frame */
        if (catchee.hitLag == 0)
            catchee.setCurr(cpoint.vaction);
        if (injury != 0) {
            catchee.hpLost(Math.abs(injury), false);
            injury  = 0;
        }
        LFcpoint cp = catchee.currFrame.cpoint;
        catchee.faceRight = catcher.faceRight ^ cpoint.changedir;
        catchee.px = catcher.faceRight ?
                ((catcher.px - catcher.currFrame.centerR + cpoint.x) + (cp.x - catchee.currFrame.centerR)):
                ((catcher.px + catcher.currFrame.centerR - cpoint.x) - (cp.x - catchee.currFrame.centerR));
        catchee.py =
                ((catcher.py - catcher.currFrame.centerY + cpoint.y) - (cp.y - catchee.currFrame.centerY));
        catcherUpdated = false;
        return;
    }

}

package ecosystem;

import base.Region;

public interface Terrain {

  /**
   * Check for unlimited mode.
   *
   * @return {@code true} if unlimited mode is turned on
   */
  boolean isUnlimitedMode();

  /**
   * Calculates the velocity after friction deduction.
   * The default friction factor is 1.
   *
   * @param v initial velocity
   * @return result velocity
   */
  default double applyFriction(double v) {
    return v >= 0.0 ? Math.max(0.0, v - 1.0) : Math.min(0.0, v + 1.0);
  }

  /**
   * Calculates the landing velocity after friction deduction.
   * The default landing friction factor is 3.
   *
   * @param v initial velocity
   * @return result velocity
   */
  default double applyLandingFriction(double v) {
    return v / 3.0;
  }

  /**
   * Calculates the velocity after gravity effect.
   * The default gravity factor is 1.7.
   *
   * @param vy initial y-velocity
   * @return result y-velocity
   */
  default double getGravity() {
    return 1.7;
  }

  /**
   * Gets current timestamp.
   *
   * @return current timestamp
   */
  int getTimestamp();

  /**
   * Returns valid xz-boundary of {@code Hero}es.
   *
   * @return a {@code Region} whose {@code y1} and {@code y2} have no use
   */
  Region getHeroBoundary();

  /**
   * Returns valid xz-boundary of non-{@code Hero}es.
   *
   * @return a {@code Region} whose {@code y1} and {@code y2} have no use
   */
  Region getItemBoundary();

  Terrain NULL_TERRAIN = new Terrain() {
    @Override public boolean isUnlimitedMode() { return false; }
    @Override public int getTimestamp() { throw new UnsupportedOperationException(); }
    @Override public Region getHeroBoundary() { return Region.EMPTY; }
    @Override public Region getItemBoundary() { return Region.EMPTY; }
  };

  // https://www.lf-empire.de/lf2-empire/data-changing/reference-pages/186-sound-list
  // m_cancel.wav   Menu Sound - Abort
  // m_end.wav      End 1-1 / 2-2 / VS - Battle
  // m_join.wav     Menu Sound - Select
  // m_ok.wav       Menu Sound - Enter
  // m_pass.wav     Stage-End
  // 001.wav   Punch
  // 002.wav   Block Punch
  // 003.wav   Run Tick
  // 004.wav   Run Tok
  // 005.wav   Whirlwind Casting Voom
  // 006.wav   Super Hit Bish
  // 007.wav   Woob Swing Sound
  // 008.wav   Wow Swing Sound
  // 009.wav   Pick up/stop running
  // 010.wav   Boulder hit
  // 011.wav   Bat hit
  // 012.wav   Run Ted
  // 013.wav   Free Hostage
  // 014.wav   Grab Attack
  // 015.wav   Grab
  // 016.wav   Drop
  // 017.wav   Jump
  // 018.wav   John Energy Ball Charge
  // 019.wav   Shoot John Energy Ball
  // 020.wav   Explode, energy ball hit and palm
  // 021.wav   Boulder Break
  // 022.wav   Pull Arrow
  // 023.wav   Ice Sword hit
  // 024.wav   Arrow Shot
  // 025.wav   Arrow ground hit
  // 026.wav   Shurinken ting
  // 027.wav   Weapon ting
  // 028.wav   Rudolf Blades Shing
  // 029.wav   Rudolf Keep Blades
  // 030.wav   Rudolf Shadow/Mirror Image
  // 031.wav   Louis Thunderpunch/Julian Soulpunch
  // 032.wav   Bloody Attack
  // 033.wav   Bloody Heavy Attack
  // 034.wav   Weapon ting
  // 035.wav   Crate hit
  // 036.wav   Crate break
  // 037.wav   Crate hit
  // 038.wav   Baseball
  // 039.wav   Baseball hit by bat
  // 040.wav   Potion break
  // 041.wav   Poion hit
  // 042.wav   Drink
  // 043.wav   Deep Energy Blast
  // 044.wav   Jack Flipkick hor!
  // 045.wav   Deep Dashing Strafe HAR
  // 046.wav   Dennis Energy Ball
  // 047.wav   Woody Energy Blast
  // 048.wav   Davis Energy Ball
  // 049.wav   Jan Angels/Heal
  // 050.wav   Casting Heal
  // 051.wav   Energy Shield Sound
  // 052.wav   Casting Energy Shield
  // 053.wav   Energy Shield Gone
  // 054.wav   Knight Swing Attack 1
  // 055.wav   Woody Tiger Dash
  // 056.wav   Henry Flute 1
  // 057.wav   Henry Flute 2
  // 058.wav   Henry Flute 3 (long)
  // 059.wav   Critical Arrow
  // 060.wav   Multi-Arrow
  // 061.wav   Rudolf Invisible
  // 062.wav   Rudolf Clone
  // 063.wav   Rudolf Transform
  // 064.wav   Ice Ball
  // 065.wav   Iced
  // 066.wav   No longer Iced
  // 067.wav   Fire ball
  // 068.wav   Burnt
  // 069.wav   Punch miss
  // 070.wav   Inferno
  // 071.wav   Inferno
  // 072.wav   Icicle
  // 073.wav   Whirlwind
  // 074.wav   Summon Ice Sword
  // 075.wav   Summon Energy Disk
  // 076.wav   Energy Disk Fly
  // 077.wav   Dennis Chasing Ball
  // 078.wav   Teleport
  // 079.wav   Scyte Ting
  // 080.wav   Command COME
  // 081.wav   Command STAY
  // 082.wav   Command MOVE
  // 083.wav   Armor small hit
  // 084.wav   Armor just hit
  // 085.wav   Armor class
  // 086.wav   Bat Lazer
  // 087.wav   Bat's bats
  // 088.wav   Bat's bats hit
  // 089.wav   Julian Big Bang and Soul Bomb Explode
  // 090.wav   Julian Skull Blast Hit
  // 091.wav   Summon Skull Blast Projectile
  // 092.wav   Firzen Cannon shooting
  // 093.wav   Stage Command I JOIN YOU
  // 094.wav   Bat break
  // 095.wav   Davis Uppercut YAH!!!
  // 096.wav   Henry Dragon Palm HUNK!!!
  // 097.wav   Louis Palm HEI!!!
  // 098.wav   Monk Palm HULK!
  // 099.wav   Woody Tiger Dash HOI!!!
  // 100.wav   Firzen charge firzen cannon HAI!!!
  // 101.wav   Henry Flute 4
  // 102.wav   Henry Flute 5 (long)

}

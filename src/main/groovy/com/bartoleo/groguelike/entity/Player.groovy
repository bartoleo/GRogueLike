package com.bartoleo.groguelike.entity

/**
 * base for entities (player, enemies, objects...)
 */
public class Player extends Entity {

    public int hp
    public int hpMax
    public void takeTurn(){
        return
    }

    def moveOrAttack(int pDeltaX, int pDeltaY) {
        this.x += pDeltaX
        this.y += pDeltaY
    }
}

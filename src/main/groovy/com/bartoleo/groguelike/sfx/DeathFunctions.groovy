package com.bartoleo.groguelike.sfx

import com.bartoleo.groguelike.entity.Entity
import com.bartoleo.groguelike.game.Game
import com.bartoleo.groguelike.game.GameState
import com.bartoleo.groguelike.graphic.MessageLog
import squidpony.squidcolor.SColor

class DeathFunctions {


    public static Closure playerDeath = { Entity owner ->

        MessageLog.send("${owner.name} is dead.", SColor.RED, [owner])
        Game.state = GameState.dead
        owner.ch = '%'
        owner.color = SColor.BLOOD_RED
        owner.priority = 80
        owner.faction = null
        if (owner.inventory) {
            owner.inventory.dump()
        }
    }

}

package com.bartoleo.groguelike.item

import com.bartoleo.groguelike.entity.Entity
import com.bartoleo.groguelike.graphic.MessageLog
import squidpony.squidcolor.SColor

class Item {
    public Entity owner
    Closure useFunction

    public Item(params) {
        useFunction = params?.useFunction
    }

    /**
     *
     * @return true if the item should be used up
     */
    public boolean useItem(Entity user) {

        if (owner.equipment) {
            owner.equipment.toggleEquip(user)
        } else if (useFunction) {
            return useFunction(user)
        } else {
            MessageLog.send("${owner.name} cannot be used.", SColor.RED, [user])
            return false
        }
    }

    public boolean useHeldItem(Entity user) {
        if (useFunction) {
            return useFunction(user)
        } else {
            MessageLog.send("${owner.name} cannot be used.", SColor.RED, [user])
            return false
        }
    }

}

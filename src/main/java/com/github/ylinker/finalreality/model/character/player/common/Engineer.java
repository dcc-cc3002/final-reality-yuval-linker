package com.github.ylinker.finalreality.model.character.player.common;

import com.github.ylinker.finalreality.model.character.player.AbstractPlayerCharacter;
import com.github.ylinker.finalreality.model.weapon.IWeapon;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A class that holds all the information of a single engineer character of the game.
 *
 * @author Ignacio Slater Muñoz.
 * @author Yuval Linker Groisman
 */
public class Engineer extends AbstractPlayerCharacter {
    /**
     * Creates a new engineer character.
     *
     * @param name
     *     the character's name
     * @param health
     *      The character's initial health points
     * @param attack
     *      The character's initial attack
     * @param defense
     *      The character's initial defense
     */
    public Engineer(@NotNull String name, final int health, final int attack, final int defense) {
        super(name, health, attack, defense);
    }

    @Override
    public boolean equip(IWeapon weapon) {
        if(isAlive()) {
            IWeapon myWeapon = weapon.equipToEngineer();
            if (myWeapon != null) {
                this.equippedWeapon = myWeapon;
                this.attack = this.baseAttack + myWeapon.getDamage();
                this.weight = this.baseWeight + myWeapon.getWeight();
                return true;
            }
        }
        return false;
    }

    /**
     * Makes a unique hashcode for this engineer
     * @return an int that is the hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    /**
     * Test another object to see if it is equal to this engineer
     * @param o
     *    The other object
     * @return true if it is equal, false otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Engineer)) {
            return false;
        }
        final Engineer that = (Engineer) o;
        return getName().equals(that.getName());
    }
}

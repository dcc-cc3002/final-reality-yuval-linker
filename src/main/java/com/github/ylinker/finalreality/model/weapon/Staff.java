package com.github.ylinker.finalreality.model.weapon;

/**
 * A Class that represents a Staff
 *
 * @author Yuval Linker Groisman
 */
public class Staff extends Weapon {

    private final int magicDamage;

    public Staff(final String name, final int damage, final int weight,
                 final int magicDamage) {
        super(name, damage, weight, WeaponType.STAFF);
        this.magicDamage = magicDamage;
    }

    public int getMagicDamage() { return magicDamage; }
}

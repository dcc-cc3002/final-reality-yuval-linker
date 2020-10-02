package com.github.ylinker.finalreality.model.weapon;

/**
 * A Class that represents a Sword
 *
 * @author Yuval Linker Groisman
 */
public class Sword extends Weapon {

    public Sword(final String name, final int damage, final int weight) {
        super(name, damage, weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sword)) {
            return false;
        }
        final Sword sword = (Sword) o;
        return getDamage() == sword.getDamage() &&
                getWeight() == sword.getWeight() &&
                getName().equals(sword.getName());
    }

}

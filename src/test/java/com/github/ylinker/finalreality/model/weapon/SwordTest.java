package com.github.ylinker.finalreality.model.weapon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SwordTest extends AbstractWeaponTest {

    @BeforeEach
    void swordSetUp() {
        testWeapon = new Sword("swordTest", DAMAGE, WEIGHT);
    }

    @Test
    void constructorTest(){
        checkConstruction(new Sword("swordTest", DAMAGE, WEIGHT),
                testWeapon,
                new Sword("Test", DAMAGE, WEIGHT),
                new Sword("swordTest", DIFF_DAMAGE, WEIGHT),
                new Sword("swordTest", DAMAGE, DIFF_WEIGHT),
                new Bow("bow", DAMAGE, WEIGHT));
    }
}

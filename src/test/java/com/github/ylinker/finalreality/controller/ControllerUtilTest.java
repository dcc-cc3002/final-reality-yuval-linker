package com.github.ylinker.finalreality.controller;

import com.github.ylinker.finalreality.controller.phase.BeginTurnPhase;
import com.github.ylinker.finalreality.controller.phase.SelectActionPhase;
import com.github.ylinker.finalreality.controller.phase.SelectAttackingTargetPhase;
import com.github.ylinker.finalreality.model.character.Enemy;
import com.github.ylinker.finalreality.model.character.ICharacter;
import com.github.ylinker.finalreality.model.character.IPlayerCharacter;
import com.github.ylinker.finalreality.model.weapon.IWeapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerUtilTest {
    private GameController testController;

    @BeforeEach
    void setUp() {
        testController = new GameController();
        testController.setScene(new NullScene());
    }

    @Test
    void winConditionTest() {
        assertTrue(testController.loseCondition());
        assertTrue(testController.winCondition());
        testController.createEnemy("testEnemy", 12, 12, 12, 10);
        assertFalse(testController.winCondition());
        assertTrue(testController.loseCondition());
        testController.createEngineer("testCharacter", 12, 12, 12);
        assertFalse(testController.loseCondition());
        assertFalse(testController.winCondition());
        testController.getEnemies().clear();
        assertTrue(testController.winCondition());
        assertFalse(testController.loseCondition());
    }

    @Test
    void addToQueueTest() {
        testController.createKnight("testKnight", 10, 10, 10);
        testController.createEnemy("testEnemy", 10, 10, 10, 20);
        BlockingQueue<ICharacter> queue = testController.getQueue();
        assertTrue(queue.isEmpty());
        IPlayerCharacter knight = testController.getCharacters().get(0);
        Enemy enemy = testController.getEnemies().get(0);
        knight.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
        enemy.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
        testController.addToQueue(knight);
        // On an empty queue the characters should immediately start their turn
        testController.toAttackPhase();
        testController.tryToAttack(enemy);
        testController.toBeginTurnPhase();
        assertFalse(queue.contains(knight));
        assertTrue(queue.isEmpty());
        knight.shutdownScheduledExecutor();
        testController.addToQueue(enemy);
        enemy.shutdownScheduledExecutor();
        testController.toAttackPhase();
        testController.tryToAttack(knight);
        testController.toBeginTurnPhase();
        assertFalse(queue.contains(enemy));
        assertTrue(queue.isEmpty());

        // Add dummy so that queue is not empty
        Enemy dummy = new Enemy("dummy", 1, 1, 1, 10);
        queue.add(dummy);

        knight.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
        enemy.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
        testController.addToQueue(knight);
        assertTrue(testController.getQueue().contains(knight));
        testController.addToQueue(enemy);
        assertTrue(testController.getQueue().contains(enemy));
    }

    @Test
    void turnInitTest() {
        // Create characters
        testController.createKnight("testKnight", 10, 10, 10);
        testController.createEnemy("testEnemy", 10, 10, 10, 20);
        testController.initTurns();
        assertFalse(testController.getQueue().isEmpty());
        assertEquals(2, testController.getQueue().size());
    }

    @Test
    void waitTurnTest() {
        testController.createEngineer("testEngineer", 32, 39, 49);
        testController.createEnemy("testEnemy", 83, 49, 20, 20);
        testController.createKnight("dummy", 22, 43, 43);
        BlockingQueue<ICharacter> queue = testController.getQueue();
        IPlayerCharacter engineer = testController.getCharacters().get(0);
        IPlayerCharacter dummy = testController.getCharacters().get(1);
        Enemy enemy = testController.getEnemies().get(0);
        // Add dummy to queue so that turn doesnt begin instantly
        queue.add(dummy);

        // Schedulers should be null
        assertNull(engineer.getScheduledExecutor());
        assertNull(enemy.getScheduledExecutor());

        testController.waitTurn(enemy);
        testController.waitTurn(engineer);
        assertNotNull(engineer.getScheduledExecutor());
        assertNotNull(enemy.getScheduledExecutor());
        try {
            Thread.sleep(1100);
            assertTrue(engineer.getScheduledExecutor().isShutdown());
            assertTrue(testController.getQueue().contains(engineer));
            assertFalse(enemy.getScheduledExecutor().isShutdown());
            assertFalse(testController.getQueue().contains(enemy));
            Thread.sleep(1000);
            assertTrue(enemy.getScheduledExecutor().isShutdown());
            assertTrue(testController.getQueue().contains(enemy));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testEnemyTurn() {
        testController.createEnemy("testEnemy", 20, 20, 10, 10);
        testController.createKnight("testKnight", 20, 10, 10);
        Enemy enemy = testController.getEnemies().get(0);
        IPlayerCharacter knight = testController.getCharacters().get(0);
        knight.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
        testController.waitTurn(enemy);
        try {
            Thread.sleep(500);
            assertEquals(20, testController.getCharacterHealth(knight));
            Thread.sleep(600);
            assertEquals(enemy, testController.getCurrentTurnCharacter());
            testController.tryToAttack(knight);
            testController.toBeginTurnPhase();
            assertNull(testController.getCurrentTurnCharacter());
            assertEquals(10, testController.getCharacterHealth(knight));
            assertNotNull(enemy.getScheduledExecutor());
            Thread.sleep(500);
            assertTrue(testController.getQueue().isEmpty());
            assertNull(testController.getCurrentTurnCharacter());
            assertEquals(10, testController.getCharacterHealth(knight));
            assertNotNull(enemy.getScheduledExecutor());
            Thread.sleep(600);
            assertEquals(enemy, testController.getCurrentTurnCharacter());
            testController.tryToAttack(knight);
            assertEquals(0, testController.getCharacters().size());
            testController.toBeginTurnPhase();
            assertNull(testController.getCurrentTurnCharacter());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void beginTurnTest() {
        // When queue is empty begin turn does nothing
        assertTrue(testController.getQueue().isEmpty());
        testController.createEnemy("test", 5, 10, 10, 10);
        testController.createEngineer("characterTest", 5, 10, 5);
        IPlayerCharacter engineer = testController.getCharacters().get(0);
        Enemy enemy = testController.getEnemies().get(0);
        testController.beginTurn();
        // Nothing should happen since queue is empty, so no enemy attack
        assertEquals(5, testController.getCharacterHealth(engineer));
        // Now we put a character on queue. We test that its turn starts instantly
        // This means that he is pulled from the queue
        testController.getQueue().add(engineer);
        testController.beginTurn();
        assertEquals(SelectActionPhase.class, testController.getPhase().getClass());
        testController.toAttackPhase();
        testController.tryToAttack(enemy);
        assertEquals(SelectAttackingTargetPhase.class, testController.getPhase().getClass());
        testController.toBeginTurnPhase();
        assertEquals(BeginTurnPhase.class, testController.getPhase().getClass());
        assertNotNull(engineer.getScheduledExecutor());
        try {
            testController.getQueue().add(enemy);
            Thread.sleep(1100);
            // Now the turn should not begin instantly since the queue is not empty
            assertTrue(testController.getQueue().contains(engineer));
            assertTrue(testController.getQueue().contains(enemy));
            // Now we begin the enemy's turn that should kill the engineer
            testController.beginTurn();
            assertEquals(SelectAttackingTargetPhase.class, testController.getPhase().getClass());
            testController.tryToAttack(engineer);
            assertFalse(testController.getCharacters().contains(engineer));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void invalidBeginTurnTest() {
        testController.createKnight("test", 5, 10, 10);
        testController.createEngineer("characterTest", 5, 10, 5);
        IPlayerCharacter engineer = testController.getCharacters().get(1);
        IPlayerCharacter knight = testController.getCharacters().get(0);
        testController.getQueue().add(engineer);
        testController.getQueue().add(knight);
        assertEquals(BeginTurnPhase.class, testController.getPhase().getClass());
        testController.beginTurn();
        assertEquals(engineer, testController.getCurrentTurnCharacter());
        assertEquals(SelectActionPhase.class, testController.getPhase().getClass());
        testController.setPhase(new BeginTurnPhase());
        testController.beginTurn();
        assertEquals(BeginTurnPhase.class, testController.getPhase().getClass());
        assertEquals(engineer, testController.getCurrentTurnCharacter());
        knight.beginTurn();
        assertEquals(BeginTurnPhase.class, testController.getPhase().getClass());
        assertEquals(engineer, testController.getCurrentTurnCharacter());
    }

    @Test
    void turnProcessTest() {
        testController.createEnemy("test", 20, 10, 10, 20);
        testController.createEngineer("engineer", 10, 10, 5);
        testController.createAxe("axe", 10, 10);
        IPlayerCharacter engineer = testController.getCharacters().get(0);
        Enemy enemy = testController.getEnemies().get(0);
        IWeapon axe = testController.getInventory().get(0);
        BlockingQueue<ICharacter> queue = testController.getQueue();
        testController.initTurns();
        // Every character is put on the queue randomly
        assertFalse(queue.isEmpty());
        try {
            if(testController.getCurrentTurnCharacter() == enemy) {
                assertEquals(SelectAttackingTargetPhase.class, testController.getPhase().getClass());
                testController.tryToAttack(engineer);
                testController.toBeginTurnPhase();
                assertNotNull(enemy.getScheduledExecutor());
                assertFalse(queue.contains(enemy));
                assertEquals(SelectActionPhase.class, testController.getPhase().getClass());
                testController.toAttackPhase();
                testController.tryToAttack(enemy);
                testController.toBeginTurnPhase();
                assertFalse(queue.contains(engineer));
                assertEquals(BeginTurnPhase.class, testController.getPhase().getClass());
                Thread.sleep(1100);
                // Should be the engineer's turn again
                assertEquals(engineer, testController.getCurrentTurnCharacter());
                testController.toAttackPhase();
                Thread.sleep(1000);
                // Since turn is not over when enemy gos into queue again it should not start its turn
                assertEquals(testController.getCurrentTurnCharacter(), engineer);
                // Enemy turn again and it kills engineer
                testController.tryToAttack(enemy);
                testController.toBeginTurnPhase();
                testController.tryToAttack(engineer);
                assertEquals(0, engineer.getHealth());
            } else {
                assertEquals(SelectActionPhase.class, testController.getPhase().getClass());
                testController.toAttackPhase();
                testController.tryToAttack(enemy);
                assertFalse(queue.contains(engineer));
                assertNotNull(engineer.getScheduledExecutor());
                testController.toBeginTurnPhase();
                assertEquals(SelectAttackingTargetPhase.class, testController.getPhase().getClass());
                assertEquals(testController.getCurrentTurnCharacter(), enemy);
                testController.tryToAttack(engineer);
                testController.toBeginTurnPhase();
                assertTrue(queue.isEmpty());
                assertEquals(BeginTurnPhase.class, testController.getPhase().getClass());
                Thread.sleep(1100);
                assertEquals(testController.getCurrentTurnCharacter(), engineer);
                testController.toEquipPhase();
                testController.tryToEquip(axe);
                Thread.sleep(1100);
                assertEquals(testController.getCurrentTurnCharacter(), engineer);
                testController.toAttackPhase();
                testController.tryToAttack(enemy);
                testController.toBeginTurnPhase();
                assertEquals(testController.getCurrentTurnCharacter(), enemy);
                testController.tryToAttack(engineer);
                assertEquals(0, engineer.getHealth());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void checkRandomTarget() {
        testController.createEngineer("engineer", 10, 10, 5);
        testController.createAxe("axe", 10, 10);
        ICharacter character = testController.chooseRandomTarget();
        assertEquals(character, testController.getLastAttackedCharacter());
        assertTrue(testController.getCharacters().contains(character));
    }
}

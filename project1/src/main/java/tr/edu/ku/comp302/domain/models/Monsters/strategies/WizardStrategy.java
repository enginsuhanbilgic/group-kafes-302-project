package tr.edu.ku.comp302.domain.models.monsters.strategies;

import tr.edu.ku.comp302.domain.models.monsters.WizardMonster;
import tr.edu.ku.comp302.domain.controllers.MonsterController;
import tr.edu.ku.comp302.domain.controllers.BuildObjectController;
import tr.edu.ku.comp302.domain.models.Player;

import java.io.Serializable;

/**
 * WizardStrategy interface.
 * Each concrete strategy controls how the wizard should behave based on time conditions.
 */
public interface WizardStrategy extends Serializable {
    /**
     * Called every game update for this wizard. 
     *
     * @param wizard                Reference to the wizard monster itself.
     * @param player                Reference to the hero (player).
     * @param monsterController     Access to the monster controller (to remove the wizard, or help with random teleports, etc).
     * @param buildObjectController Access to build objects so we can move the rune if needed.
     * @param timePassed            In-game clock in seconds (how long we have been in this hall).
     * @param initialTime           The initial time (initialTime).
     * @param timeRemaining         How many seconds remain in the timer.
     */
    void updateBehavior(
            WizardMonster wizard,
            Player player,
            MonsterController monsterController,
            BuildObjectController buildObjectController,
            int timePassed,
            int initialTime,
            int timeRemaining
    );
}
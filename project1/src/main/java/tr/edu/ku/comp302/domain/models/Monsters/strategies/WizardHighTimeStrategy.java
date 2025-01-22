package tr.edu.ku.comp302.domain.models.monsters.strategies;

import tr.edu.ku.comp302.domain.models.monsters.WizardMonster;
import tr.edu.ku.comp302.domain.controllers.MonsterController;
import tr.edu.ku.comp302.domain.controllers.BuildObjectController;
import tr.edu.ku.comp302.domain.models.Player;

import java.io.Serializable;

/**
 * If more than 70% time remains:
 *  - Move the rune location every 3 seconds
 *  - If the time percentage later drops below 70,
 *    the MonsterController will change this strategy to Mid/Low.
 */
public class WizardHighTimeStrategy implements WizardStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public void updateBehavior(
            WizardMonster wizard,
            Player player,
            MonsterController monsterController,
            BuildObjectController buildObjectController,
            int timePassed,
            int initialTime,
            int timeRemaining
    ) {
        // Every 3 seconds, move the rune
        if ((timePassed - wizard.getLastRuneTeleportTime()) >= 3) {
            buildObjectController.transferRune();
            wizard.setLastRuneTeleportTime(timePassed);
            System.out.println("Wizard (HighTime) teleported the rune!");
        }
        // We do NOT remove the wizard automatically here;
        // it stays active unless the time percentage changes <70%,
        // in which case MonsterController changes the strategy.
    }
}
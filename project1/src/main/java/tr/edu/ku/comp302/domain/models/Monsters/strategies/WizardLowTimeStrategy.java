package tr.edu.ku.comp302.domain.models.Monsters.strategies;

import tr.edu.ku.comp302.domain.models.Monsters.WizardMonster;
import tr.edu.ku.comp302.domain.controllers.MonsterController;
import tr.edu.ku.comp302.domain.controllers.BuildObjectController;
import tr.edu.ku.comp302.domain.models.Player;

/**
 * If less than 30% time remains:
 *  - Teleport the player to a random empty location once
 *  - Then the wizard disappears immediately.
 */
public class WizardLowTimeStrategy implements WizardStrategy {

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
        // Perform the teleport exactly once if not done yet
        if (!wizard.hasTeleportedPlayer()) {
            monsterController.teleportPlayerToRandomEmptyLocation(player);
            wizard.setHasTeleportedPlayer(true);
        } else{
            int spawnTime = wizard.getSpawnTime(); 
            if (timePassed - spawnTime >= 2) {
                wizard.setShouldDisappear(true);
            }
        }
    }
}

package tr.edu.ku.comp302.domain.models.monsters.strategies;

import tr.edu.ku.comp302.domain.models.monsters.WizardMonster;
import tr.edu.ku.comp302.domain.controllers.MonsterController;
import tr.edu.ku.comp302.domain.controllers.BuildObjectController;
import tr.edu.ku.comp302.domain.models.Player;

/**
 * If less than 30% time remains:
 *  - Teleport the player to a random empty location once
 *  - Then the wizard disappears immediately.
 */
public class WizardLowTimeStrategy implements WizardStrategy {
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
        int startedMidTime = wizard.getStrategyStartTime(); 
        if (!wizard.hasTeleportedPlayer()) {
            if(timePassed - startedMidTime >= 1){
                monsterController.teleportPlayerToRandomEmptyLocation(player);
                wizard.setHasTeleportedPlayer(true);
            }
        } else{
            if (timePassed - startedMidTime >= 2) {
                wizard.setShouldDisappear(true);
            }
        }
    }
}
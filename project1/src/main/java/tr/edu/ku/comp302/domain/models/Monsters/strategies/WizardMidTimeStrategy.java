package tr.edu.ku.comp302.domain.models.monsters.strategies;

import tr.edu.ku.comp302.domain.models.monsters.WizardMonster;
import tr.edu.ku.comp302.domain.controllers.MonsterController;
import tr.edu.ku.comp302.domain.controllers.BuildObjectController;
import tr.edu.ku.comp302.domain.models.Player;

/**
 * If between 30%-70% time remains:
 *  - The wizard is indecisive: stands in place then 
 *    disappears after 2 seconds of existence (spawn).
 *  - If time ratio later changes to <30 or >70,
 *    MonsterController will switch the wizard's strategy.
 */
public class WizardMidTimeStrategy implements WizardStrategy {
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
        // Check if wizard has been here for more than 2 seconds
        int startedMidTime = wizard.getStrategyStartTime(); 
        if (timePassed - startedMidTime >= 2) {
            wizard.setShouldDisappear(true);
        }
    }
}
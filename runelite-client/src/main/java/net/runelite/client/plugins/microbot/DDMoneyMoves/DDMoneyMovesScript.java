package net.runelite.client.plugins.microbot.DDMoneyMoves;

import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnaceScript;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import java.util.concurrent.TimeUnit;

public class DDMoneyMovesScript extends Script {
    public static double version = 1.0;
    public boolean run(DDMoneyMovesConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                //DDBlastFurnaceScript.masterOnSwitch= true;
                sleep(1000);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

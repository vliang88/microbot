package net.runelite.client.plugins.microbot.DDAdvertiser;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.TimeUnit;


public class DDAdvertiserScript extends Script {
    public static double version = 1.0;
    WorldPoint POHWorldPoint = new WorldPoint(2955 , 3224, 0);

    public boolean run(DDAdvertiserConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {

                //Check if player is within range of the POHWorldPoint
                if(Rs2Player.getWorldLocation().distanceTo2D(POHWorldPoint) > 0){
                    //Not inside the area for advertising, Walk to advertising place
                    if(!Rs2Player.isWalking()) {
                        Rs2Walker.walkTo(POHWorldPoint, 0);
                    }else{
                        Rs2Player.toggleRunEnergy(true);
                    }
                }else{
                    //We are inside of advertising area, time to start typing out the messages
                    int randomMessageChooser = Random.random(1,4);
                    switch (randomMessageChooser){
                        case 1:
                            Rs2Keyboard.typeString(config.message1());
                            Rs2Keyboard.enter();
                            break;
                        case 2:
                            Rs2Keyboard.typeString(config.message2());
                            Rs2Keyboard.enter();
                            break;
                        case 3:
                            Rs2Keyboard.typeString(config.message3());
                            Rs2Keyboard.enter();
                            break;
                        default:
                            System.out.println("Invalid message selection " + randomMessageChooser);
                            break;
                    }
                    sleep(config.pauseMinTime(),config.pauseMaxTime());
                }


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

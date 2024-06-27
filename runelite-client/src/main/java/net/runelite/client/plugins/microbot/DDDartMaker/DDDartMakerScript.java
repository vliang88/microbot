package net.runelite.client.plugins.microbot.DDDartMaker;

import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.DDDartMaker.DDDartMakerConfig;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;


public class DDDartMakerScript extends Script {
    public static double version = 1.0;
    public static boolean masterOnSwitch = false;
    public WorldPoint anvilWP = new WorldPoint(3188,3427,0);
    public WorldPoint bankWP = new WorldPoint(3185,3436,0);
    public static long lastXpDropMillsecond = 0;
    public static int currentSmithingXp;
    int initXP = 0;
    public boolean run(DDDartMakerConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if(!masterOnSwitch)return;

                if(Microbot.isGainingExp ||
                        Rs2Player.isAnimating() || Rs2Player.isMoving()){
                    return;
                }

                if(!Rs2Inventory.contains(2351)){
                    if(Rs2GameObject.get("bank") == null) {
                        Rs2Walker.walkTo(bankWP);
                    }else{
                        Rs2Bank.openBank();
                    }
                }else{ //Contains bar inside inventory
                    if(Rs2GameObject.get("anvil") == null){
                        Rs2Walker.walkTo(anvilWP);
                    }else{
                        Rs2GameObject.interact("Anvil", "Smith");
                        sleep(600,1200);
                        sleepUntil(() -> Rs2Widget.hasWidget("What would you like"));
                        Rs2Widget.clickWidget(20447261);
                        sleepUntil(() -> !Microbot.isGainingExp);
                    }
                }

                if(Rs2Bank.isOpen()){
                    if(Rs2Inventory.get("hammer") == null){
                        Rs2Bank.withdrawOne("hammer");
                    }
                    Rs2Bank.depositAllExcept("hammer");
                    if(!Rs2Bank.hasBankItem("iron bar")) {
                        masterOnSwitch = false;
                    }
                    Rs2Bank.withdrawAll("iron bar");
                    sleepUntilTrue(Rs2Inventory::isFull,600,1200);
                    Rs2Bank.closeBank();
                }


            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

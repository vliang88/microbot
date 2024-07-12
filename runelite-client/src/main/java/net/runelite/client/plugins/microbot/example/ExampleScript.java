package net.runelite.client.plugins.microbot.example;

import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnaceScript;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.concurrent.TimeUnit;


public class ExampleScript extends Script {
    enum plankMakerStates{
        state_pm_init,
        state_pm_doBank,
        state_pm_teleportToHouse,
        state_pm_makePlanks,
        state_pm_teleportToBank,
    }

    public static ExampleScript.plankMakerStates currentState = ExampleScript.plankMakerStates.state_pm_init;
    public static double version = 1.0;
    public static WorldPoint lumbyWP = new WorldPoint(3222,3218, 0);

    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if(Microbot.getClient().getEnergy() > 20 && Rs2Player.isMoving()){
                    Rs2Player.toggleRunEnergy(true);
                }
                Microbot.getClient().setCameraPitchTarget(383);

                switch(currentState) {
                    case state_pm_init:
                        if (Rs2Player.getWorldLocation().distanceTo2D(lumbyWP) < 5) {
                            if (Microbot.getClient().getWorld() != 539) {
                                Microbot.hopToWorld(539);
                                if(Rs2Widget.hasWidget("Switch World")){
                                    Rs2Widget.clickWidget("Switch World");
                                }
                                break;
                            }else {
                                currentState = plankMakerStates.state_pm_doBank;
                            }
                        } else {
                            if (Microbot.getClient().getWorld() != 539) {
                                Rs2Walker.walkTo(lumbyWP);
                            } else {
                                Microbot.hopToWorld(330);
                            }
                        }
                        break;
                    case state_pm_doBank:
                        if(!Rs2Bank.isOpen()){
                            Rs2GameObject.interact(10586, "Use"); //Lumby PVP bank chest
                            sleep(250,500);
                            sleepUntil(() -> Rs2Bank.isOpen());
                            break;
                        }else{
                            if(Rs2Inventory.hasItem("plank")) {
                                Rs2Bank.depositAllExcept("Coins", "Law rune", "Dust rune");
                                break;
                            }
                            //Bank is open. We need to withdraw the necessary
                            // - Dust Rune x50
                            // - Law Rune x50
                            // - Coins x100k
                            // - Rest Logs
                            if(!Rs2Inventory.hasItemAmount(ItemID.DUST_RUNE, 50)){
                                Rs2Bank.withdrawX(ItemID.DUST_RUNE, 50);
                                break;
                            }
                            if(!Rs2Inventory.hasItemAmount(ItemID.LAW_RUNE, 50)){
                                Rs2Bank.withdrawX(ItemID.LAW_RUNE, 50);
                                break;
                            }
                            if(!Rs2Inventory.hasItemAmount("Coins", 50000)){
                                Rs2Bank.withdrawX("Coins", 150000);
                                break;
                            }
                            if(!Rs2Inventory.isFull()){
                                Rs2Bank.withdrawAll(ItemID.MAHOGANY_LOGS);
                            }else{
                                Rs2Bank.closeBank();
                                currentState = plankMakerStates.state_pm_teleportToHouse;
                            }
                        }
                        break;
                    case state_pm_teleportToHouse:
                        if (Rs2Player.getWorldLocation().distanceTo2D(lumbyWP) < 5) {
                            Rs2Magic.cast(MagicAction.TELEPORT_TO_HOUSE);
                            sleep(250,500);
                            sleepUntil(()-> Rs2Player.getWorldLocation().distanceTo2D(lumbyWP) > 5);
                        }else{
                            currentState = plankMakerStates.state_pm_makePlanks;
                        }
                        break;
                    case state_pm_makePlanks:
                        if(Rs2Npc.getNpc("Demon butler") == null || !Rs2Inventory.isFull()){
                            currentState = plankMakerStates.state_pm_teleportToBank;
                            break;
                        }
                        if(Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(Rs2Npc.getNpc("Demon butler").getWorldLocation()) > 3){
                            //call the servant
                            while(Rs2Tab.getCurrentTab() != InterfaceTab.SETTINGS) {
                                Rs2Tab.switchToSettingsTab();
                            }
                            if(!Rs2Widget.getWidget(116, 2).getText().equalsIgnoreCase("Controls Settings")){
                                Rs2Widget.clickWidget(116,63);
                                sleep(600,1200);
                                break;
                            }
                            if(Rs2Widget.getWidget(116, 2).getText().equalsIgnoreCase("Controls Settings")){
                                Rs2Widget.clickWidget(116, 31);
                                sleep(600, 1200);
                                sleepUntil(()-> Rs2Widget.getWidget(370, 0).getText().equalsIgnoreCase("House Options"));
                                Rs2Widget.clickWidget(370, 22);
                                sleep(600,1200);
                                break;
                            }
                        }else{
                            if(Rs2Widget.hasWidget("Take to sawmill")){
                                Rs2Widget.clickWidget("Take to sawmill");
                                sleep(250,500);
                                break;
                            }
                            if(Rs2Widget.hasWidget("Click here to continue")){
                                Rs2Widget.clickWidget("Click here to continue");
                                sleep(250,500);
                                break;
                            }
                            if(Rs2Widget.hasWidget("Yes")){
                                Rs2Widget.clickWidget("Yes");
                                sleep(250,500);
                                break;
                            }
                            if(Rs2Widget.hasWidget("Okay, here's 10,000 coins.")){
                                Rs2Widget.clickWidget("Okay, here's 10,000 coins.");
                                sleep(250,500);
                                break;
                            }
                            if(Rs2Inventory.hasItem("logs")){
                                Rs2Inventory.interact("logs");
                                sleep(600,1200);
                                Rs2Npc.interact("Demon butler", "Use");
                                sleep(250,500);
                                break;
                            }
                            if(Rs2Widget.hasWidget("Sawmill")){
                                Rs2Widget.clickWidget("Sawmill");
                                sleep(250,500);
                                sleepUntil(()-> Rs2Widget.hasWidget("Enter Amount"));
                                Rs2Keyboard.typeString(Integer.toString(Rs2Inventory.get("logs").quantity));
                                Rs2Keyboard.enter();
                                break;
                            }
                            if(Rs2Tab.getCurrentTab() != InterfaceTab.INVENTORY){
                                Rs2Tab.switchToInventoryTab();
                            }
                        }
                        break;
                    case state_pm_teleportToBank:
                        if(Rs2GameObject.exists(13616)){
                            Rs2GameObject.interact(13616);
                            sleep(1200,200);
                            sleepUntil(() -> !Rs2Player.isMoving());
                            sleep(250,500);
                        }
                        if(!Rs2GameObject.exists(13616)) {
                            currentState = plankMakerStates.state_pm_doBank;
                        }
                        break;
                    default:
                        sleep(600);
                        break;
                    }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 250, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        currentState = plankMakerStates.state_pm_init;
        super.shutdown();
    }
}

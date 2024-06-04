package net.runelite.client.plugins.microbot.DDBurnerLighter;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


enum states{
    state_init,
    state_walkToHousePortal,
    state_atPortal,
    state_atHouse,
    state_atPhails
}
public class DDBurnerLighterScript extends Script {
    public static double version = 1.0;
    public long incenseBurnDurationTick;
    public long scriptStartTimestamp;
    public long lightStateTimestamp;
    //ITEM IDS
    public static int notedMarentillId = 252;
    public static int marentillId = 251;
    public static int tinderboxId = 590;
    public static int coinId = 995;
    public static int POHId = 15478;
    public static int POHExitId = 4525;
    public static int burnerId = 13213;
    public static int unlitBurnerId = 13212;
    public static int alterId = 40879;

    //WORLD POINTS
    WorldPoint POHWorldPoint = new WorldPoint(2955 , 3224, 0);
    WorldPoint PhialsWorldPoint = new WorldPoint(2950 , 3214, 0);
    public int POHWorldPointSize = 5;
    public static boolean fullyInit = false;
    //Script states
    states currentState = states.state_init;
    public boolean run(DDBurnerLighterConfig config) {
        Microbot.enableAutoRunOn = false;
        incenseBurnDurationTick = 200 + Microbot.getClient().getRealSkillLevel(Skill.FIREMAKING);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                //TODO:Check to make sure that the account has at least 35FM or else return
                //Make sure we not going nuts when the player is walking
                while(Rs2Player.isWalking()){
                    sleep(100);
                }
                switch(currentState) {
                    case state_init:
                        //make sure we are on world 330
                        if (Microbot.getClient().getWorld() != 330) {
                            Microbot.hopToWorld(330);
                            sleep(10000); //Let it sleep for 10 sec
                        }
                        //Change the minimap zoom and make top down camera
                        Microbot.getClient().setMinimapZoom(2.0);
                        Microbot.getClient().setScalingFactor(272);
                        Rs2Camera.setPitch(100);
                        //Open up inventory
                        if(!Rs2Inventory.isOpen())
                            Rs2Inventory.open();

                        //check to make sure we have all item and choose next state
                        if (!Rs2Inventory.contains(notedMarentillId) || !Rs2Inventory.contains("Coins") || !Rs2Inventory.contains(tinderboxId)) {
                            System.out.println("Inventory Missing Item");
                        }else{
                            //All guest houses are plane 1
                            if(!isInHouse()){
                                currentState = states.state_walkToHousePortal;
                            }else{
                                currentState = states.state_atHouse;
                            }
                        }
                        break;
                    case state_walkToHousePortal:
                        //Walk to the portal if we are not currently at the portal
                        if(Rs2Player.getWorldLocation().distanceTo2D(POHWorldPoint) > POHWorldPointSize) {
                            //Not inside the area for advertising, Walk to advertising place
                            if (!Rs2Player.isWalking()) {
                                Rs2Walker.walkTo(POHWorldPoint, 0);
                            }else{
                                if(Microbot.getClient().getEnergy() > 20){
                                    Rs2Player.toggleRunEnergy(true);
                                }
                            }
                        }else{//We are currently at the portal, go to state to see if we go into house
                            currentState = states.state_atPortal;
                        }
                        break;
                    case state_atPortal:
                        //Make sure we are at the portal area in this state or we go back
                        if(Rs2Player.getWorldLocation().distanceTo2D(POHWorldPoint) > POHWorldPointSize) {
                            currentState = states.state_walkToHousePortal;
                            break;
                        }
                        //Check if we have more than 2 maretill in our inventory
                        if(Rs2Inventory.count(marentillId) >=2 ){
                            //We have enough, lets go into a portal
                            enterPortal();
                            currentState = states.state_atHouse;
                        }else{
                            //We dont have enough herbs, go unnote
                            if(!Rs2Npc.hasLineOfSight(Rs2Npc.getNpc("Phials"))){
                                Rs2Walker.walkTo(PhialsWorldPoint, 0);
                            }
                            currentState = states.state_atPhails;
                        }
                        break;
                    case state_atPhails:
                        unoteMarentill();
                        if(!Rs2GameObject.exists(POHId)) {
                            Rs2Walker.walkTo(POHWorldPoint);
                        }
                        currentState = states.state_atPortal;
                        break;

                    case state_atHouse:
                        if(!isInHouse()){
                            currentState = states.state_walkToHousePortal;
                            break;
                        }
                        if(Rs2Inventory.count(marentillId) >=2 ){
                            sleep(1000);
                        }else{
                            exitHousePortal();
                            currentState = states.state_atPortal;
                        }
                        break;
                    default:
                        break;
                }
                System.out.println("CurrentState: " + currentState);
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

    private void unoteMarentill(){
        //Click on the noted maretill in inventory
        Rs2Inventory.interact(notedMarentillId, "Use");
        sleep(250,1000);
        Rs2Npc.interact(NpcID.PHIALS, "Use");
        sleep(250,1000);
        sleepUntil(() -> Rs2Widget.hasWidget("Exchange All"));
        sleep(250,1000);
        Rs2Widget.clickWidget("Exchange All");
    }
    private void enterPortal(){
        Rs2GameObject.interact(POHId, "Friend's House");
        sleep(250,1000);
        sleepUntil(() -> Rs2Widget.hasWidget("Enter Name"));
        sleep(250,1000);
        Rs2Keyboard.typeString("workless");
        Rs2Keyboard.enter();
        sleepUntil(()->Rs2Player.getWorldLocation().getPlane() != 0);
    }
    private void exitHousePortal(){
        Rs2Camera.turnTo(Rs2GameObject.findObjectById(POHExitId));
        Rs2GameObject.interact(POHExitId, "Enter");
        sleepUntil(()->Rs2Player.getWorldLocation().getPlane() == 0);
    }
    private boolean isInHouse(){
        return Rs2GameObject.exists(POHExitId) || Rs2GameObject.exists(alterId)
                || Rs2GameObject.exists(burnerId) || Rs2GameObject.exists(unlitBurnerId);
    }
}

package net.runelite.client.plugins.microbot.DDAccountBuilder;

import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class ThievingTrainingSubScript {
    enum theivingState{
        init,
        goToTheivingZone,
        doTheiving
    }
    static theivingState currentState = theivingState.init;
    public static WorldPoint lumbyWP = new WorldPoint(3222,3218, 0);
    public static WorldPoint teaStallWP = new WorldPoint(3268,3410, 0);
    public static void run() {
        try {
            System.out.println("crafting Sub State: " + currentState);
            switch (currentState) {
                case init:
                    if (!Rs2Bank.isOpen()) {
                        Rs2Bank.openBank();
                    } else {
                        Rs2Bank.depositAll();
                        Rs2Bank.closeBank();
                        currentState = theivingState.goToTheivingZone;
                    }
                    break;
                case goToTheivingZone:
                    if (Rs2Player.getRealSkillLevel(Skill.THIEVING) < 5) {
                        NPC npc = Rs2Npc.getNpc(NpcID.MAN_3108);
                        if(Rs2Npc.hasLineOfSight(npc)){
                            currentState = theivingState.doTheiving;
                            break;
                        }
                        if (Rs2Tab.getCurrentTab() != InterfaceTab.MAGIC) {
                            Rs2Tab.switchToMagicTab();
                            sleep(600);
                            break;
                        }
                        Rs2Widget.clickWidget(218, 7);
                        sleep(1200);
                        sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(lumbyWP) < 10, 25000);
                    }else{
                        if(Rs2GameObject.exists(635)){
                            currentState = theivingState.doTheiving;
                        }else{
                            Rs2Walker.walkTo(teaStallWP);
                        }
                    }
                    break;
                case doTheiving:
                    if (Rs2Player.getRealSkillLevel(Skill.THIEVING) < 5) {
                        Rs2Npc.interact("Man", "Pickpocket");
                        if(Rs2Inventory.get(ItemID.COIN_POUCH).quantity == 28){
                            Rs2Inventory.interact(ItemID.COIN_POUCH,"Open-all");
                        }
                        currentState = theivingState.goToTheivingZone;
                    }else{
                        if(Rs2Inventory.contains(ItemID.COIN_POUCH)){
                            Rs2Inventory.interact(ItemID.COIN_POUCH,"Open-all");
                        }
                        if(Rs2Inventory.isFull()){
                            Rs2Inventory.dropAll();
                        }
                        if(Rs2GameObject.exists(635)){
                            Rs2GameObject.interact(635,"Steal-from");
                            sleep(1200);
                            sleepUntil(()-> !Rs2Player.isAnimating());
                        }
                    }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    public static void Shutdown(){
        currentState = theivingState.init;
    }
}

package net.runelite.client.plugins.microbot.DDAccountBuilder;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.globval.WidgetIndices;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class MagicTrainingSubScript {
        enum magicState{
            init,
            doBank,
            buySupplies,
            goToTower,
            setUpMagic,
            trainMagic,
        }
        static magicState currentState = magicState.init;
        public static WorldPoint towerWP = new WorldPoint(3110,3159, 2);
        static int mindRuneInBank = 0;
        static int airStaffInBank = 0;
        static boolean magicSet = false;
        public static void run() {
            try {
                System.out.println("Magic Sub State: " + currentState);
                switch (currentState) {
                    case init:
                        if (Rs2Player.getWorldLocation().distanceTo(BankLocation.GRAND_EXCHANGE.getWorldPoint()) > 10) {
                            Rs2GrandExchange.walkToGrandExchange();
                        }else{
                           currentState = magicState.doBank;
                        }
                        break;
                    case doBank:
                        if(!Rs2Bank.isOpen()){
                            Rs2Bank.openBank();
                        }else{
                            if(Rs2Bank.hasItem(ItemID.MIND_RUNE)){
                                mindRuneInBank = Rs2Bank.findBankItem("Mind rune").quantity;
                            }
                            if(Rs2Bank.hasItem(ItemID.STAFF_OF_AIR)) {
                                airStaffInBank = Rs2Bank.findBankItem("Staff of air").quantity;
                            }
                            if(airStaffInBank == 0  || mindRuneInBank < 3000){
                                currentState = magicState.buySupplies;
                            }else{
                                Rs2Bank.withdrawOne(ItemID.STAFF_OF_AIR);
                                Rs2Bank.withdrawAll(ItemID.MIND_RUNE);
                                if(!Rs2Inventory.contains(ItemID.STAFF_OF_AIR,ItemID.MIND_RUNE)){
                                    break;
                                }
                                Rs2Bank.closeBank();
                                currentState = magicState.goToTower;
                            }
                        }
                        break;
                    case buySupplies:
                        if(airStaffInBank == 0 ){
                            Rs2GrandExchange.buyItem("Staff of air", 5000, 1);
                        }
                        if(mindRuneInBank < 3000){
                            Rs2GrandExchange.buyItem("Mind rune", 5, 3000);
                        }
                        sleepUntil(MagicTrainingSubScript::geIsComplete);
                        Rs2GrandExchange.collectToBank();
                        currentState = magicState.doBank;
                        break;
                    case goToTower:
                        if(Rs2Player.getWorldLocation().distanceTo(towerWP) > 3){
                            Rs2Walker.walkTo(towerWP);
                        }else{
                            currentState = magicState.setUpMagic;
                        }
                        break;
                    case setUpMagic:
                        if(Rs2Inventory.contains(ItemID.STAFF_OF_AIR)) {
                            Rs2Inventory.interact(ItemID.STAFF_OF_AIR, "Wear");
                            sleepUntil(() -> Rs2Equipment.hasEquipped(ItemID.STAFF_OF_AIR));
                        }
                        if(!magicSet){
                            //Set air strike
                            if(Rs2Tab.getCurrentTab() != InterfaceTab.COMBAT) {
                                Rs2Tab.switchToCombatOptionsTab();
                            }
                            if(Rs2Widget.getWidget(593,26) != null){
                                Rs2Widget.clickWidget(593,26);
                            }
                            if(Rs2Widget.getWidget(13172737).getChild(1) != null){
                                Rs2Widget.clickChildWidget(13172737,1);
                                Rs2Tab.switchToInventoryTab();
                                magicSet = true;
                                currentState = magicState.trainMagic;
                            }
                        }
                        break;
                    case trainMagic:
                        NPC lesser = Rs2Npc.getNpc(NpcID.LESSER_DEMON);
                        if(!Microbot.isGainingExp && Rs2Npc.hasLineOfSight(lesser)){
                            Rs2Npc.interact(lesser,"Attack");
                            sleep(1200);
                        }
                        break;
                }
            }catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    private static boolean geIsComplete(){
        GrandExchangeOffer[] offers = Microbot.getClient().getGrandExchangeOffers();
        for (GrandExchangeOffer offer : offers){
            if(offer.getState() == GrandExchangeOfferState.BUYING)
                return false;
        }
        return true;
    }
    public static void Shutdown(){
        magicSet = false;
        currentState = magicState.init;
    }
}

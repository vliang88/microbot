package net.runelite.client.plugins.microbot.DDAccountBuilder;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class CraftingTrainingSubScript {
    static int chiselInBank = 0;
    static int uncutOpalInBank = 0;
    enum craftingState{
        init,
        buySupplies,
        doBank,
        doCrafting
    }
    static craftingState currentState = craftingState.init;
    public static void run() {
        try {
            System.out.println("crafting Sub State: " + currentState);
            switch(currentState){
                case init:
                    if(Rs2Player.getWorldLocation().distanceTo(BankLocation.GRAND_EXCHANGE.getWorldPoint()) > 10){
                        Rs2GrandExchange.walkToGrandExchange();
                    }
                    if(!Rs2Bank.isOpen()){
                        Rs2Bank.openBank();
                    }else{
                        currentState = craftingState.doBank;
                    }
                    break;
                case doBank:
                    if(!Rs2Bank.isOpen()){
                        Rs2Bank.openBank();
                        break;
                    }
                    Rs2Bank.depositAll();
                    chiselInBank = Rs2Bank.getCount(ItemID.CHISEL);
                    uncutOpalInBank = Rs2Bank.getCount(ItemID.UNCUT_OPAL);
                    if(chiselInBank == 0 || uncutOpalInBank == 0){
                        currentState = craftingState.buySupplies;
                    }else{
                        Rs2Bank.withdrawOne(ItemID.CHISEL);
                        Rs2Bank.withdrawAll(ItemID.UNCUT_OPAL);
                        Rs2Bank.closeBank();
                        currentState = craftingState.doCrafting;
                    }
                    break;
                case buySupplies:
                    if(!Rs2GrandExchange.isOpen()){
                        Rs2GrandExchange.openExchange();
                    }else{
                        if(chiselInBank == 0)
                            Rs2GrandExchange.buyItem("Chisel", 1000, 1);
                        if(uncutOpalInBank == 0)
                            Rs2GrandExchange.buyItem("Uncut opal", 1000, 200);
                        sleepUntil(CraftingTrainingSubScript::geIsComplete);
                        Rs2GrandExchange.collectToBank();
                        currentState = craftingState.doBank;
                    }
                    break;
                case doCrafting:
                    if(!Microbot.isGainingExp) {
                        if(Rs2Inventory.contains(ItemID.UNCUT_OPAL) && Rs2Inventory.contains(ItemID.CHISEL)) {
                            Rs2Inventory.combine(ItemID.CHISEL, ItemID.UNCUT_OPAL);
                            sleep(1200);
                            if (Rs2Widget.getWidget(270, 14) != null) {
                                Rs2Widget.clickWidget(270, 14);
                                sleep(1200);
                            }
                        }else{
                            currentState = craftingState.doBank;
                        }
                    }

                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
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
        currentState = craftingState.init;
    }
}

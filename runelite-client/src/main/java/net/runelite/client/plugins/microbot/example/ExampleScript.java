package net.runelite.client.plugins.microbot.example;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.grandexchange.GrandExchangeClient;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.plugins.grandexchange.GrandExchangeSearchMode;
import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnaceScript;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.WidgetIndices;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.http.api.ge.GrandExchangeTrade;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;


public class ExampleScript extends Script {
    enum plankMakerStates{
        state_pm_init,
        state_pm_doBank,
        state_pm_teleportToHouse,
        state_pm_makePlanks,
        state_pm_teleportToBank,
        state_goToGe,
        state_withdrawPlanks,
        state_sellPlanks,
        state_buySupplies,
        state_goToLumby,
    }

    public static plankMakerStates currentState = plankMakerStates.state_goToGe;
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
                            if((!Rs2Bank.hasBankItem("Mahogany logs", 28) &&
                                    !Rs2Bank.hasBankItem("Oak logs", 28)) ||
                                    !Rs2Bank.hasBankItem("Coins", 50000)){
                                Rs2Bank.depositAllExcept( "Law rune", "Dust rune");
                                Rs2Bank.withdrawOne("Fire rune");
                                Rs2Bank.closeBank();
                                Microbot.hopToWorld(386);
                                currentState = plankMakerStates.state_goToGe;
                                break;
                            }
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
                                if(Rs2Bank.hasBankItem("Mahogany logs", 28)) {
                                    Rs2Bank.withdrawAll(ItemID.MAHOGANY_LOGS);
                                }else{
                                    Rs2Bank.withdrawAll(ItemID.OAK_LOGS);
                                }
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
                    case state_goToGe:
                        if (Microbot.getClient().getWorld() != 386) {
                            Microbot.hopToWorld(386);
                            break;
                        }
                        if (!Rs2Walker.isInArea(BankLocation.GRAND_EXCHANGE.getWorldPoint(), 5)) {
                            if (Rs2Magic.canCast(MagicAction.VARROCK_TELEPORT)) {
                                Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);
                                sleep(250,500);
                                sleepUntil(()-> Rs2Player.getWorldLocation().distanceTo2D(lumbyWP) > 5);
                            } else {
                                if (!Rs2Player.isMoving()) {
                                    Rs2Walker.restartPathfinding(Rs2Player.getWorldLocation(), new WorldPoint(3161, 3490, 0));
                                }
                                Rs2Walker.walkTo(new WorldPoint(3161, 3490, 0));
                                break;
                            }
                        }else{
                            currentState = plankMakerStates.state_withdrawPlanks;
                        }
                        break;
                    case state_withdrawPlanks:
                        if(!Rs2Bank.isOpen()){
                            Rs2Bank.openBank();
                            break;
                        }else{
                            Rs2Bank.setWithdrawAsNote();
                            Rs2Bank.withdrawAll("plank");
                            Rs2Bank.withdrawAll("Coins");
                            if(!Rs2Bank.hasBankItem("plank")){
                                Rs2Bank.closeBank();
                                currentState = plankMakerStates.state_sellPlanks;
                            }
                        }
                        break;
                    case state_sellPlanks:
                        if(!Rs2GrandExchange.isOpen()){
                            Rs2GrandExchange.openExchange();
                            break;
                        }else{
                            for (Rs2Item item : Rs2Inventory.items()) {
                                if (!item.isTradeable()) continue;
                                if(item.getName().contains("plank")) {
                                    Rs2GrandExchange.sellItemUnder5Percent(item.getName());
                                }
                            }
                            sleep(10000,15000);
                            Rs2GrandExchange.collectToInventory();
                            currentState = plankMakerStates.state_buySupplies;

                        }
                        break;
                    case state_buySupplies:
                        if(!Rs2GrandExchange.isOpen()){
                            Rs2GrandExchange.openExchange();
                            break;
                        }else{
                            //calculate how much of mahogany we can buy with our moneys
                            int mahoganyLogToBuy = calculateLogsToBuy("mahogany", Rs2Inventory.get("Coins").quantity);
                            if(mahoganyLogToBuy != 0 && !itemAlreadyTradingInGE(ItemID.MAHOGANY_LOGS)) {
                                Rs2GrandExchange.buyItemAbove5Percent("Mahogany logs", mahoganyLogToBuy);
                                sleep(5000, 6000);
                                Rs2GrandExchange.collectToBank();
                            }
                            if(mahoganyLogToBuy == 11000 || itemAlreadyTradingInGE(ItemID.MAHOGANY_LOGS)) {
                                int oakLogToBuy = 0;
                                if(mahoganyLogToBuy == 11000) {
                                    oakLogToBuy = calculateLogsToBuy("oak", Rs2Inventory.get("Coins").quantity - 17050000);
                                }
                                if(itemAlreadyTradingInGE(ItemID.MAHOGANY_LOGS)) {
                                    oakLogToBuy = calculateLogsToBuy("oak", Rs2Inventory.get("Coins").quantity);
                                }
                                if (oakLogToBuy != 0 && !itemAlreadyTradingInGE(ItemID.OAK_LOGS)){
                                    Rs2GrandExchange.buyItemAbove5Percent("Oak logs", oakLogToBuy);
                                    sleep(5000, 6000);
                                    Rs2GrandExchange.collectToBank();
                                }
                            }
                            currentState = plankMakerStates.state_goToLumby;
                        }
                        break;
                    case state_goToLumby:
                        if (Rs2Player.getWorldLocation().distanceTo2D(lumbyWP) < 5) {
                            currentState = plankMakerStates.state_pm_init;
                        }else{
                            Rs2Bank.openBank();
                            if(!Rs2Bank.hasBankItem("Mahogany logs", 28) ||
                                    !Rs2Bank.hasBankItem("Oak logs", 28)){
                                if(geIsComplete()){
                                    Rs2GrandExchange.collectToBank();
                                }
                                break;
                            }
                            if(Rs2Magic.canCast(MagicAction.LUMBRIDGE_TELEPORT)){
                                Rs2Magic.cast(MagicAction.LUMBRIDGE_TELEPORT);
                                sleep(600,1200);
                                sleepUntil(()-> Rs2Player.getWorldLocation().distanceTo2D(BankLocation.GRAND_EXCHANGE.getWorldPoint()) > 10);
                            }else{
                                Rs2Bank.openBank();
                                if(!Rs2Inventory.hasItemAmount(ItemID.DUST_RUNE,3)){
                                    Rs2Bank.withdrawX(ItemID.DUST_RUNE, 3 - Rs2Inventory.count(ItemID.DUST_RUNE));
                                }
                                if(!Rs2Inventory.contains(ItemID.LAW_RUNE)){
                                    Rs2Bank.withdrawOne(ItemID.LAW_RUNE);
                                }
                                Rs2Bank.closeBank();
                            }
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
        currentState = plankMakerStates.state_goToGe;
        super.shutdown();
    }

    int calculateLogsToBuy(String logs, int totalGP){
        int logPrice, sawmillPrice, butlerFee, logAmtToBuy = 0;
        switch(logs){
            case "oak":
                logPrice = getGEPrice("Oak logs");
                sawmillPrice = 250;
                butlerFee = 50;
                logAmtToBuy = (totalGP / (logPrice + sawmillPrice + butlerFee));
                if (logAmtToBuy > 15000) logAmtToBuy = 15000;
                break;
            case "teak":
                logPrice = getGEPrice("Teak logs");
                sawmillPrice = 500;
                butlerFee = 50;
                logAmtToBuy = (totalGP / (logPrice + sawmillPrice + butlerFee));
                if (logAmtToBuy > 13000) logAmtToBuy = 13000;
                break;
            case "mahogany":
                logPrice = getGEPrice("Mahogany logs");
                sawmillPrice = 1500;
                butlerFee = 50;
                logAmtToBuy = (totalGP / (logPrice + sawmillPrice + butlerFee));
                if (logAmtToBuy > 11000) logAmtToBuy = 11000;
                break;
            default:
                break;
        }
        return logAmtToBuy;
    }

    int getGEPrice(String itemName) {
        Pair<GrandExchangeSlots, Integer> slot = Rs2GrandExchange.getAvailableSlot();
        Widget buyOffer = Rs2GrandExchange.getOfferBuyButton(slot.getLeft());
        if (buyOffer == null) return 0;

        Microbot.getMouse().click(buyOffer.getBounds());
        sleepUntil(Rs2GrandExchange::isOfferTextVisible);
        sleepUntil(() -> Rs2Widget.hasWidget("What would you like to buy?"));
        if (Rs2Widget.hasWidget("What would you like to buy?"))
            Rs2Keyboard.typeString(itemName);
        sleepUntil(() -> Rs2Widget.hasWidget(itemName)); //GE Search Results
        sleep(1200, 1600);
        Pair<Widget, Integer> itemResult = Rs2GrandExchange.getSearchResultWidget(itemName);
        if (itemResult != null) {
            Rs2Widget.clickWidgetFast(itemResult.getLeft(), itemResult.getRight(), 1);
            sleepUntil(() -> !Rs2Widget.hasWidget("Choose an item..."));
            sleep(600, 1600);
        }
        Widget pricePerItemButton5Percent = Rs2GrandExchange.getPricePerItemButton_Plus5Percent();
        if (pricePerItemButton5Percent != null) {
            Microbot.getMouse().click(pricePerItemButton5Percent.getBounds());
            sleep(600,1200);
        }
        return Rs2GrandExchange.getItemPrice();
    }

    boolean itemAlreadyTradingInGE(int itemId){
        GrandExchangeOffer[] offers = Microbot.getClient().getGrandExchangeOffers();
        for (GrandExchangeOffer offer : offers){
            if(offer.getItemId() == itemId)
                return true;
        }
        return false;
    }

    boolean geIsComplete(){
        GrandExchangeOffer[] offers = Microbot.getClient().getGrandExchangeOffers();
        for (GrandExchangeOffer offer : offers){
            if(offer.getState() == GrandExchangeOfferState.BOUGHT)
                return true;
        }
        return false;
    }
}

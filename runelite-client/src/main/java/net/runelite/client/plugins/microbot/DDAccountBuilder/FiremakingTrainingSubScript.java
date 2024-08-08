package net.runelite.client.plugins.microbot.DDAccountBuilder;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class FiremakingTrainingSubScript {
    static int TinderboxInBank = 0;
    static int LogsInBank = 0;
    static int logsRequired = 0;

    enum firemakingState {
        init,
        buySupplies,
        doBank,
        goToStartTile,
        doFiremaking
    }

    static FiremakingTrainingSubScript.firemakingState currentState = FiremakingTrainingSubScript.firemakingState.init;

    public static WorldPoint[] startingTile= {
            new WorldPoint(3196,3491,0),
            new WorldPoint(3196,3490,0),
            new WorldPoint(3196,3489,0),
            new WorldPoint(3196,3488,0),
    };
    static int startPointIndex = 0;

    public static void run() {
        try {
            System.out.println("Firemaking Sub State: " + currentState);
            switch (currentState) {
                case init:
                    if(Rs2Player.getWorldLocation().distanceTo(BankLocation.GRAND_EXCHANGE.getWorldPoint()) > 10){
                        Rs2GrandExchange.walkToGrandExchange();
                    }
                    if(!Rs2Bank.isOpen()){
                        Rs2Bank.openBank();
                    }else{
                        currentState = firemakingState.doBank;
                    }
                    break;
                case doBank:
                    if(!Rs2Bank.isOpen()){
                        Rs2Bank.openBank();
                        break;
                    }
                    Rs2Bank.depositAllExcept(ItemID.TINDERBOX);
                    TinderboxInBank = Rs2Bank.getCount(ItemID.TINDERBOX)+ Rs2Inventory.count(ItemID.TINDERBOX);
                    LogsInBank = Rs2Bank.getCount(ItemID.LOGS);
                    logsRequired = (Experience.getXpForLevel(20) -
                            Microbot.getClient().getSkillExperience(Skill.FIREMAKING))/14;
                    if(TinderboxInBank == 0 || LogsInBank < logsRequired){
                        currentState = firemakingState.buySupplies;
                    }else{
                        if(Rs2Inventory.count(ItemID.TINDERBOX) == 0)
                            Rs2Bank.withdrawOne(ItemID.TINDERBOX);
                        Rs2Bank.withdrawAll(ItemID.LOGS);
                        sleepUntil(Rs2Inventory::isFull);
                        if(Rs2Inventory.isFull()) {
                            Rs2Bank.closeBank();
                            currentState = firemakingState.goToStartTile;
                        }
                    }
                    break;
                case buySupplies:
                    if(!Rs2GrandExchange.isOpen()){
                        Rs2GrandExchange.openExchange();
                    }else{
                        if(TinderboxInBank == 0)
                            Rs2GrandExchange.buyItemGePrice("Tinderbox", 1);
                        if(LogsInBank < logsRequired)
                            Rs2GrandExchange.buyItem("Logs", 1000,logsRequired - LogsInBank);
                        sleepUntil(FiremakingTrainingSubScript::geIsComplete);
                        Rs2GrandExchange.collectToBank();
                        currentState = FiremakingTrainingSubScript.firemakingState.doBank;
                    }
                    break;
                case goToStartTile:
                    if(Rs2Player.getWorldLocation().distanceTo(startingTile[startPointIndex]) > 3){
                        Rs2Walker.walkTo(startingTile[startPointIndex]);
                    }else{
                        //Check to make sure the tile does no already have fire,
                        // if it does go to next start tile and repeat.
                        Rs2Walker.walkCanvas(startingTile[startPointIndex]);
                        List<GameObject> fires = Rs2GameObject.getGameObjects()
                                .stream()
                                .filter(x -> x.getId() == 26185)
                                .sorted(Comparator.comparingInt(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation())))
                                .collect(Collectors.toList());
                        if (!fires.isEmpty()) {
                            if (Rs2Player.getWorldLocation().distanceTo(fires.get(0).getWorldLocation()) == 0) {
                                startPointIndex = (startPointIndex+1)%4;
                                Rs2Walker.walkTo(startingTile[startPointIndex]);
                                Rs2Walker.walkCanvas(startingTile[startPointIndex]);
                                break;
                            }
                        }
                        currentState = firemakingState.doFiremaking;
                    }
                    break;
                case doFiremaking:
                    //if the inventory is full go to start point
                    if(!Rs2Player.isAnimating()) {
                        if(Rs2Inventory.contains(ItemID.LOGS) && Rs2Inventory.contains(ItemID.TINDERBOX)) {
                            List<GameObject> fires = Rs2GameObject.getGameObjects()
                                    .stream()
                                    .filter(x -> x.getId() == 26185)
                                    .sorted(Comparator.comparingInt(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation())))
                                    .collect(Collectors.toList());
                            if (!fires.isEmpty()) {
                                if (Rs2Player.getWorldLocation().distanceTo(fires.get(0).getWorldLocation()) == 0) {
                                    startPointIndex = (startPointIndex+1)%4;
                                    Rs2Walker.walkTo(startingTile[startPointIndex]);
                                    Rs2Walker.walkCanvas(startingTile[startPointIndex]);
                                    break;
                                }
                            }
                            Rs2Inventory.combine(ItemID.TINDERBOX, ItemID.LOGS);
                            sleep(600);
                            sleepUntil(() -> !Rs2Player.isAnimating() || !Microbot.isGainingExp);
                        }else{
                            currentState = firemakingState.doBank;
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
        currentState = firemakingState.init;
    }
}


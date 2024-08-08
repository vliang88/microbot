package net.runelite.client.plugins.microbot.DDAccountBuilder;

import net.runelite.api.*;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.WidgetIndices;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;


public class DDAccountBuilderScript extends Script {
    public static double version = 1.0;
    public int[] suppliesToBuy = {
            ItemID.WATER_RUNE, 3000,
            ItemID.EARTH_RUNE, 3000,
            ItemID.FIRE_RUNE, 3000,
            ItemID.MIND_RUNE, 1500,
            ItemID.CHAOS_RUNE, 1500,
            ItemID.IRON_PICKAXE, 1,
            ItemID.STEEL_PICKAXE, 1,
            ItemID.MITHRIL_PICKAXE, 1,
            ItemID.ADAMANT_PICKAXE, 1,
            ItemID.RUNE_PICKAXE, 1,
            ItemID.LOBSTER, 100,
            ItemID.CHISEL, 1,
            ItemID.UNCUT_OPAL, 200,
            ItemID.TINDERBOX, 1,
            ItemID.LOGS, 68,
            ItemID.IRON_BAR, 2,
            ItemID.REDBERRY_PIE, 1};

    enum AccBuilderStates{
        changeWorld,
        decideSkilltoTrain,
        getMember,
        trainCrafting,
        trainFiremaking,
        trainMagic,
        trainTheiving,
        trainMining,
    }
    public static AccBuilderStates currentState = AccBuilderStates.decideSkilltoTrain;
    int WorldToLogInto = 494;

    public boolean run(DDAccountBuilderConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) return;
                if (Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN){
                    new Login(WorldToLogInto);
                    return;
                }
                if(Microbot.getClient().getEnergy() > 1000 /*&& Rs2Player.isMoving()*/){
                    Rs2Player.toggleRunEnergy(true);
                }
                Microbot.getClient().setCameraPitchTarget(383);


                switch (currentState){
                    case decideSkilltoTrain:
                        if(!Rs2Player.isMember()){
                            currentState = AccBuilderStates.getMember;
                            break;
                        }
                        if(Rs2Player.getRealSkillLevel(Skill.CRAFTING) < 12){
                            currentState = AccBuilderStates.trainCrafting;
                            break;
                        }
                        if(Rs2Player.getRealSkillLevel(Skill.FIREMAKING) < 16){
                            currentState = AccBuilderStates.trainFiremaking;
                        }
                        if(Rs2Player.getRealSkillLevel(Skill.THIEVING) < 14){
                            currentState = AccBuilderStates.trainTheiving;
                        }
                        break;
                    case getMember:
                        //go to GE because we need to get the bond
                        if(Rs2Player.getWorldLocation().distanceTo2D(BankLocation.GRAND_EXCHANGE.getWorldPoint()) > 20){
                            Rs2GrandExchange.walkToGrandExchange();
                        }
                        //Buy a old school bond
                        if(!Rs2Inventory.hasItem(ItemID.OLD_SCHOOL_BOND_UNTRADEABLE)) {
                            Rs2Bank.openBank();
                            if(Rs2Bank.hasItem(ItemID.OLD_SCHOOL_BOND_UNTRADEABLE)){
                                Rs2Bank.withdrawAll(ItemID.OLD_SCHOOL_BOND_UNTRADEABLE);
                                Rs2Bank.closeBank();
                                break;
                            }
                            Rs2GrandExchange.openExchange();
                            Rs2GrandExchange.abortAllTrades();
                            if(Rs2GrandExchange.buyItemGePrice("Old school bond", 1) != 0) {
                                sleepUntil(Rs2GrandExchange::hasBoughtOffer,30000);
                                if(Rs2GrandExchange.hasBoughtOffer()) {
                                    Rs2GrandExchange.collectToInventory();
                                }
                            }
                        }else{
                            Rs2Inventory.interact(ItemID.OLD_SCHOOL_BOND_UNTRADEABLE,"redeem");
                            sleep(1200,5000);
                            if(Rs2Widget.getWidget(861,12) != null){
                                Rs2Widget.clickWidget(861,12);
                                sleep(1200,2400);
                            }
                            if(Rs2Widget.getWidget(289,8) != null){
                                Rs2Widget.clickWidget(289,8);
                                sleep(1200,2400);
                            }
                            if(Rs2Widget.hasWidget("Click to continue")){
                                Rs2Widget.clickWidget("Click to continue");
                            }
                            if(Rs2Widget.hasWidget("Please log out ")) {
                                WorldToLogInto = 386;
                                Rs2Player.logout();
                                currentState = AccBuilderStates.decideSkilltoTrain;
                            }
                        }
                        break;

                    case trainCrafting:
                        if(Rs2Player.getRealSkillLevel(Skill.CRAFTING) < 12) {
                            CraftingTrainingSubScript.run();
                            break;
                        }
                        currentState = AccBuilderStates.decideSkilltoTrain;
                        break;
                    case trainFiremaking:
                        if(Rs2Player.getRealSkillLevel(Skill.FIREMAKING) < 16) {
                            FiremakingTrainingSubScript.run();
                            break;
                        }
                        currentState = AccBuilderStates.decideSkilltoTrain;
                        break;
                    case trainTheiving:
                        if(Rs2Player.getRealSkillLevel(Skill.THIEVING) < 14) {
                            ThievingTrainingSubScript.run();
                            break;
                        }
                        currentState = AccBuilderStates.decideSkilltoTrain;
                        break;
                    default:
                        break;
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        currentState = AccBuilderStates.decideSkilltoTrain;
        CraftingTrainingSubScript.Shutdown();
        FiremakingTrainingSubScript.Shutdown();
        ThievingTrainingSubScript.Shutdown();
        super.shutdown();
    }
}
package net.runelite.client.plugins.microbot.DDMoneyMoves;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnaceConfig;
import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnaceScript;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

public class DDMoneyMovesScript extends Script {
    public static double version = 1.0;
    public enum moneyMovesStates{
        state_idle,
        state_walkToGE,
        state_withdrawAllAsset,
        state_sellAllAsset,
        state_buyResource,
        state_goToScriptStart
    }
    public moneyMovesStates currentStates = moneyMovesStates.state_idle;
    public boolean run(DDMoneyMovesConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                //For weird cases so we know where to go
                if(DDBlastFurnaceScript.masterOnSwitch){
                    currentStates =moneyMovesStates.state_idle;
                }
                switch (currentStates) {
                    case state_idle:
                        if(DDBlastFurnaceScript.masterOnSwitch){
                            break;
                        }
                        currentStates = moneyMovesStates.state_walkToGE;
                        break;
                    case state_walkToGE:
                        if(!Rs2Walker.isInArea(BankLocation.GRAND_EXCHANGE.getWorldPoint(),5)){
                            Rs2Walker.walkTo(BankLocation.GRAND_EXCHANGE.getWorldPoint());
                            break;
                        }else {
                            currentStates = moneyMovesStates.state_withdrawAllAsset;
                        }
                        break;
                    case state_withdrawAllAsset:
                        if(withdrawAllAssets()) {
                            currentStates = moneyMovesStates.state_sellAllAsset;
                        }
                        break;
                    case state_sellAllAsset:
                        if(liquidateAllAssets()) {
                            currentStates = moneyMovesStates.state_buyResource;
                        }
                        break;
                    case state_buyResource:
                        if(buyResources(config)){
                            currentStates = moneyMovesStates.state_goToScriptStart;
                        }
                        break;
                    case state_goToScriptStart:
                        WorldPoint GECartPoint = new WorldPoint(3141, 3504, 0);
                        if(Rs2Player.getWorldLocation().distanceTo(GECartPoint) > 5){
                            Rs2Walker.walkTo(GECartPoint);
                        }else{
                            Rs2GameObject.interact("trapdoor", "Travel");
                            sleep(600,1200);
                            sleepUntilTrue(()->(Rs2Player.getPoseAnimation() == 808 && Rs2GameObject.get("trapdoor") == null), 100, 5000);
                            DDBlastFurnaceScript.masterOnSwitch = true;
                            currentStates = moneyMovesStates.state_idle;
                        }
                        break;
                    default:
                        break;
                }
                sleep(1000);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    public boolean withdrawAllAssets(){
        if(!Rs2Bank.isOpen()){
            Rs2Bank.openBank();
        }else{
            Rs2Bank.setWithdrawAsNote();
            sleep(600,1200);
            Rs2Bank.withdrawAll("bar");
            sleep(600,1200);
            Rs2Bank.withdrawAll("coin");
            sleep(600,1200);
            Rs2Bank.closeBank();
            sleepUntilTrue(()->!Rs2Bank.isOpen(),100,1200);
            return true;
        }
        return false;
    }

    public boolean liquidateAllAssets(){
        if(!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
        }else{
            Rs2GrandExchange.sellInventory();
            sleep(10000,20000);
            Rs2GrandExchange.collect(false);
            sleep(600,1200);
            return true;
        }
        return false;
    }

    public boolean buyResources(DDMoneyMovesConfig config){
        if(!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
        }else{
            int primOreAmt = config.reloadResourceAmount();
            int secOreAmt = primOreAmt * DDBlastFurnaceScript.coalAmountPerPrim;
            int stamPotAmt = secOreAmt / 350;
            if(Rs2GrandExchange.isAllSlotsEmpty()) {
                //calulate how much of stuff we need
                Rs2GrandExchange.buyItemAbove5Percent(DDBlastFurnaceScript.primOre, primOreAmt);
                Rs2GrandExchange.buyItemAbove5Percent(DDBlastFurnaceScript.secOre, secOreAmt);
                Rs2GrandExchange.buyItemAbove5Percent("Stamina potion(4)", stamPotAmt);
                sleep(10000, 20000);
                Rs2GrandExchange.collect(true);
                Rs2GrandExchange.closeExchange();
                return true;
                /*
                if(Rs2Inventory.get(DDBlastFurnaceScript.primOre).quantity == primOreAmt ||
                        Rs2Inventory.get(DDBlastFurnaceScript.secOre).quantity == secOreAmt ||
                        Rs2Inventory.get("Stamina potion(4)").quantity == stamPotAmt){
                    Rs2GrandExchange.closeExchange();
                    return true;
                }else{
                    return false;
                }
                */
            }else{
                Rs2GrandExchange.collect(true);
                sleep(600, 1200);
                Rs2GrandExchange.closeExchange();
                return true;
                /*
                if(Rs2Inventory.get(DDBlastFurnaceScript.primOre).quantity == primOreAmt ||
                        Rs2Inventory.get(DDBlastFurnaceScript.secOre).quantity == secOreAmt ||
                        Rs2Inventory.get("Stamina potion(4)").quantity == stamPotAmt){
                    Rs2GrandExchange.closeExchange();
                    return true;
                }else{
                    return false;
                }
                */

            }
        }
        return false;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

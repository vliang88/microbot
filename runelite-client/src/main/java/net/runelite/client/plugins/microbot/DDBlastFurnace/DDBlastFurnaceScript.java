package net.runelite.client.plugins.microbot.DDBlastFurnace;

import net.runelite.api.*;
import net.runelite.api.annotations.Component;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.WidgetIndices;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static java.util.Objects.nonNull;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilOnClientThread;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

public class DDBlastFurnaceScript extends Script {
    enum blastFurnanceStates{
        state_init,
        state_doBank,
        state_payForemanAndCoffer,
        state_placeOreToConveyer,
        state_takeFromDispenser
    }
    public static double version = 2.0;
    public int coinID = 995;
    public int bucketId = 1925;
    public int bucketOfWaterId = 1929;
    public int cofferValue = 0;
    public int foremanId = 2923;
    public int cofferId = 29330;
    public int stamPot1DoseId = 12631;
    public int conveyerId = 9100;
    public int barDispenserId = 9092;
    public int coalId = 453;
    public long foremanStartMilliSecond = 0;
    public int coalBagState = 0;
    public int placeConveyerCnt = 0;
    public boolean skipTakeBar = false;
    public static long timeStart =0;
    public WorldPoint dispenserWP = new WorldPoint(1939,4963,0);
    public WorldArea blastFurnanceWA= new WorldArea(1935,4956,20,20,0);
    public static int barsSmelted = 0;
    public static int barPrice= 0;
    public static int primOrePrice = 0;
    public static int secOrePrice = 0;
    public static int stamPotSipPrice = 0;
    public static int cofferAndForemanAndSipsSpent = 0;
    public static boolean masterOnSwitch = false;
    public static blastFurnanceStates currentState = blastFurnanceStates.state_init;

    public void getPrices(DDBlastFurnaceConfig config){
        barPrice = Microbot.getItemManager().getItemPriceWithSource(config.BlastFurnaceBarSelection().getBarId(), true);
        primOrePrice = Microbot.getItemManager().getItemPriceWithSource(config.BlastFurnaceBarSelection().getPrimaryId(),true);
        secOrePrice = Microbot.getItemManager().getItemPriceWithSource(config.BlastFurnaceBarSelection().getSecondaryId(),true) * config.BlastFurnaceBarSelection().getCoalRequired();
        stamPotSipPrice = Microbot.getItemManager().getItemPriceWithSource(stamPot1DoseId, true);
    }

    public boolean run(DDBlastFurnaceConfig config) {
        Microbot.enableAutoRunOn = false;
        timeStart = System.currentTimeMillis();
        barsSmelted = 0;
        cofferAndForemanAndSipsSpent = 0;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if(!masterOnSwitch) return;
            try {
                //Make sure run is always on as long as we have 20 run energy
                if(Microbot.getClient().getEnergy() > 20){
                    Rs2Player.toggleRunEnergy(true);
                }
                if(!Rs2Walker.isInArea(dispenserWP, 30)){
                    currentState = blastFurnanceStates.state_init;
                }
                switch (currentState){
                    case state_init:
                        if(!Rs2Walker.isInArea(dispenserWP, 30)){
                            Rs2Walker.walkTo(dispenserWP);
                            sleepUntilTrue(()->!Rs2Player.isWalking(), 100, 5000);
                            break;
                        }
                        currentState = blastFurnanceStates.state_doBank;
                        break;
                    case state_doBank:
                        doBank(config);
                        if(Rs2Inventory.get(coinID) != null){
                            currentState = blastFurnanceStates.state_payForemanAndCoffer;
                        }
                        else if(Rs2Inventory.get("bar") != null)
                        {
                            break;
                        }else{
                            currentState = blastFurnanceStates.state_placeOreToConveyer;
                        }
                        break;
                    case state_payForemanAndCoffer:
                        payForemanAndCoffer(config);
                        currentState = blastFurnanceStates.state_doBank;
                        break;
                    case state_placeOreToConveyer:
                        if((Rs2Inventory.get(config.BlastFurnaceBarSelection().getPrimaryOre()) != null) ||
                                (Rs2Inventory.get(coalId) != null)){
                            if(config.useIceGlove()){
                                //We are using ice glove, Go to conveyor
                                placeOreOntoConveyer();
                            }else{
                                //We are not using ice glove
                                if(Rs2Inventory.contains(bucketId)){
                                    fillBucket();
                                    placeOreOntoConveyer();
                                }else if(Rs2Inventory.contains(bucketOfWaterId)){
                                    placeOreOntoConveyer();
                                }
                            }
                            currentState = blastFurnanceStates.state_takeFromDispenser;
                        }else{
                            currentState = blastFurnanceStates.state_doBank;
                        }
                        break;
                    case state_takeFromDispenser:
                        if(!skipTakeBar && placeConveyerCnt == 2) {
                            if(Math.random() > 0.25) {
                                Rs2Walker.walkMiniMap(dispenserWP);
                            }
                            sleepUntil(this::dispenserHasBar);
                            grabBarsFromDispenser(config.useIceGlove(),config.BlastFurnaceBarSelection().getVarbit());

                        }
                        //Corner case where we didnt get the bar for some odd reason
                        if(dispenserHasBar() && !Rs2Inventory.isFull()){
                            grabBarsFromDispenser(config.useIceGlove(),config.BlastFurnaceBarSelection().getVarbit());
                        }
                        placeConveyerCnt =0;
                        currentState = blastFurnanceStates.state_doBank;
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

    void doBank(DDBlastFurnaceConfig config){
        //Open bank if its not open
        if(!Rs2Bank.isOpen()){
            //Rs2Bank.openBank();
            if(Rs2GameObject.getGameObjects(26707) != null) {
                Rs2GameObject.interact(26707, "Use");
                sleepUntilTrue(Rs2Bank::isOpen, 100, 10000);
                return;
            }else{
                Rs2Walker.walkTo(BankLocation.BLAST_FURNACE_BANK.getWorldPoint());
            }
        }
        //Make sure we have enough ores
        if(!Rs2Bank.hasBankItem(config.BlastFurnaceBarSelection().getPrimaryOre(),30) ||
                !Rs2Bank.hasBankItem(config.BlastFurnaceBarSelection().getSecondaryOre(), 30)){//||
                //!Rs2Bank.hasBankItem(coinID, config.cofferReloadAmount())){
            Microbot.pauseAllScripts = true;
            masterOnSwitch = false;
            return;
        }
        //We have to deposit everything including bars. Leave coal bag and bucket to start new.
        Rs2Bank.depositAllExcept("Coal bag","bucket","bucket of water");
        //Check if we need to pay coffer or foreman
        int amountWithdraw = needToPayCoffer(config.cofferMinimum(),config.cofferReloadAmount()) + needToPayForeman();
        if(amountWithdraw > 0){
            cofferAndForemanAndSipsSpent += amountWithdraw;
            Rs2Bank.withdrawX(coinID, amountWithdraw);
            sleepUntilTrue(() -> Rs2Inventory.contains(coinID), 100, 5000);
            Rs2Bank.closeBank();
            return;
        }else {
            //We will be withdrawing ores here depending on our configuration
            //check if we have our coal bag, water bucket(if not using ice glove), and what we making
            if(Rs2Inventory.get("Coal bag", false) == null){
                //We dont have the bag. Withdraw it if our ore selection uses coal
                if(config.BlastFurnaceBarSelection().getCoalRequired() != 0) {
                    Rs2Bank.withdrawAll("Coal bag", false);
                }
            }else{ //we have the bag. If our selection dont need it deposit it
                if(config.BlastFurnaceBarSelection().getCoalRequired() == 0){
                    Rs2Bank.depositOne("Coal bag", false);
                }
            }
            if(!config.useIceGlove()){
                if(Rs2Inventory.get(bucketId) == null && Rs2Inventory.get(bucketOfWaterId) == null) {
                    if (Rs2Inventory.get(bucketId) == null && Rs2Bank.hasItem(bucketId)) {
                        Rs2Bank.withdrawItem(bucketId);
                        sleep(100, 250);
                        sleepUntilTrue(() -> Rs2Inventory.contains(bucketId), 100, 5000);
                    } else if (Rs2Inventory.get(bucketOfWaterId) == null) {
                        Rs2Bank.withdrawItem(bucketOfWaterId);
                        sleep(100, 250);
                        sleepUntilTrue(() -> Rs2Inventory.contains(bucketOfWaterId), 100, 5000);
                    }
                }
            }else{//We are using ice glove. Deposit bucket back into bank
                if(Rs2Inventory.get(bucketId) != null){
                    Rs2Bank.depositAll(bucketId);
                }
                if(Rs2Inventory.get(bucketOfWaterId) != null){
                    Rs2Bank.depositAll(bucketOfWaterId);
                }
            }
            //See if we are using stam pots. If yes do appropriate thing
            if(config.useStaminaPot() && needtoDrinkStamPot()){
                Rs2Bank.withdrawOne("Stamina potion", 600);
                //sleepUntil(()-> Rs2Inventory.contains("Stamina potion"));
                if(Rs2Inventory.get("Stamina") != null) {
                    if (Rs2Inventory.interact("Stamina", "Drink")) {
                        cofferAndForemanAndSipsSpent += stamPotSipPrice;
                        sleep(100, 250);
                        sleepUntilTrue(() -> !needtoDrinkStamPot(), 100, 5000);
                    }
                }
                //Deposit everything into bank except bucket and coal bag incase we dont break vials
                Rs2Bank.depositAllExcept("Coal bag","bucket","bucket of water");
                sleep(100,250);
            }
            //We have all inventory set up now we fill coal bag, withdraw required ores and go.
            //Fill coal bag
            fillCoalBag();
            sleep(100,250);
            //Figure out what we have to fill rest of inventory with.
            if(Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL) >= (config.BlastFurnaceBarSelection().getCoalRequired() * Rs2Inventory.getEmptySlots())) {
                Rs2Bank.withdrawAll(config.BlastFurnaceBarSelection().getPrimaryOre());
                skipTakeBar = false;
            }else{
                Rs2Bank.withdrawAll("coal");
                skipTakeBar = true;
            }
            sleep(250,600);
            sleepUntilTrue(() -> !Rs2Inventory.isEmpty(), 100, 5000);
            Rs2Bank.closeBank();
            sleep(100,250);
            sleepUntilTrue(() -> !Rs2Bank.isOpen(), 100, 5000);
        }
    }
    int needToPayCoffer(int cofferThreshold, int cofferReloadAmt){
        if(Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COFFER) < cofferThreshold){
            return cofferReloadAmt;
        }
        return 0;
    }
    int needToPayForeman(){
        if(Microbot.getClient().getRealSkillLevel(Skill.SMITHING) < 60){
            if(((System.currentTimeMillis() - foremanStartMilliSecond) > 570000) ||
                    (foremanStartMilliSecond == 0)){ //9min 30 second into the payment or never paid
                return 2500;
            }
        }
        return 0;
    }
    boolean needtoDrinkStamPot(){
        return Microbot.getVarbitValue(Varbits.STAMINA_EFFECT) == 0;
    }
    void payForemanAndCoffer(DDBlastFurnaceConfig config){
        //We have to pay foreman before using the coffer for <60 smithing
        if(needToPayForeman() != 0){
            Rs2Npc.interact(foremanId, "Pay");
            sleep(100,250);
            sleepUntilTrue(() -> Rs2Widget.hasWidget("Yes"), 100, 5000);
            Rs2Widget.clickWidget("Yes");
            foremanStartMilliSecond = System.currentTimeMillis();
            sleep(100,250);
            sleepUntilTrue(() -> Rs2Widget.hasWidget("Click here to Continue"), 100, 5000);
        }
        //See if we have to pay the coffer
        if(needToPayCoffer(config.cofferMinimum(), config.cofferReloadAmount()) != 0){
            Rs2GameObject.interact(cofferId, "Use");
            sleep(100,250);
            sleepUntilTrue(() -> Rs2Widget.hasWidget("Deposit coins"), 100, 5000);
            Rs2Widget.clickWidget("Deposit coins");
            sleep(100,250);
            sleepUntilTrue(() -> Rs2Widget.hasWidget("Deposit how much"), 100, 5000);
            Rs2Keyboard.typeString(Integer.toString(Rs2Inventory.get(coinID).quantity));
            Rs2Keyboard.enter();
        }
    }

    void fillBucket(){
        if(Rs2Inventory.contains(bucketId)) {
            Rs2GameObject.interact("Sink", "Fill-bucket");
            sleep(100, 250);
            sleepUntilTrue(() -> Rs2Inventory.contains(bucketOfWaterId), 100, 5000);
        }
    }
    void fillCoalBag(){
        Rs2Inventory.interact("coal bag", "Fill");
        coalBagState = 0xff;
        sleep(600,800);
        sleepUntilTrue(() ->coalBagState != 0xff, 100, 5000);
        if(coalBagState == 0){
            Rs2Inventory.interact("coal bag", "Fill");
        }
    }
    void emptyCoalBag(){
        Rs2Inventory.interact("coal bag", "Empty");
        coalBagState = 0xff;
        sleep(600,800);
        sleepUntilTrue(() ->coalBagState != 0xff, 100, 5000);
        if(coalBagState == 1){
            Rs2Inventory.interact("coal bag", "Empty");
        }
    }
    void placeOreOntoConveyer(){
        Rs2GameObject.interact(conveyerId, "Put-ore");
        sleep(250,500);
        sleepUntilTrue(() -> (!Rs2Inventory.isFull()),100,100000);
        emptyCoalBag();
        Rs2GameObject.interact(conveyerId, "Put-ore");
        sleep(600,1000);
        sleepUntilTrue(() -> placeConveyerCnt == 2, 100, 5000);
    }
    boolean dispenserHasBar(){
        return (Microbot.getVarbitValue(Varbits.BAR_DISPENSER) >= 2);
    }
    void grabBarsFromDispenser(boolean useIceGlove, int barVarbit){
        if(!useIceGlove && Microbot.getVarbitValue(Varbits.BAR_DISPENSER) != 3){
            Rs2Inventory.interact(bucketOfWaterId,"Use");
            sleep(100,500);
            Rs2GameObject.interact(barDispenserId, "Use");
            sleep(100,500);
            sleepUntilTrue(() -> Microbot.getVarbitValue(Varbits.BAR_DISPENSER) == 3, 100, 5000);
        }
        if(Microbot.getVarbitValue(Varbits.BAR_DISPENSER) >= 2) {
            Rs2GameObject.interact(barDispenserId, "take");
            sleep(100, 500);
            sleepUntilTrue(() -> Rs2Widget.getWidget(17694733) != null,100, 10000);
            if(Rs2Widget.getWidget(17694733) != null) {
                if (Math.random() > 0.5) {
                    barsSmelted += Microbot.getVarbitValue(barVarbit);
                    Rs2Widget.clickWidget(17694733);
                } else {
                    barsSmelted += Microbot.getVarbitValue(barVarbit);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                }
                //sleepUntil(Rs2Inventory::isFull);
            }
            sleep(600, 750);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

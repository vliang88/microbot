package net.runelite.client.plugins.microbot.DDBlastFurnace;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.friendlist.FriendListConfig;
import net.runelite.client.plugins.friendlist.FriendListPlugin;
import net.runelite.client.plugins.grandexchange.GrandExchangeClient;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.WidgetIndices;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilOnClientThread;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

public class DDBlastFurnaceScript extends Script {
    enum blastFurnanceStates{
        state_addFriend,
        state_BF_init,
        state_BF_doBank,
        state_BF_payForemanAndCoffer,
        state_BF_placeOreToConveyer,
        state_BF_takeFromDispenser,
        state_BF_goToKeldagrim,
        state_goToGe,
        state_sellProducts,
        state_buySupplies,
        state_decideScriptToRun,
        state_DM_init,
        state_DM_bank,
        state_DM_makeDarts,
        state_muling_goToHouse,
        state_muling_changeWorld,
        state_muling_enterHouse,
        state_muling_depositTip,
        state_muling_gotoGe,
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
    public static int POHId = 15478;
    public long foremanStartMilliSecond = 0;
    public int coalBagState = 0;
    public int placeConveyerCnt = 0;
    public boolean skipTakeBar = false;
    public static long timeStart =0;
    public WorldPoint dispenserWP = new WorldPoint(1939,4963,0);
    public WorldArea blastFurnanceWA= new WorldArea(1935,4956,20,20,0);
    public WorldPoint anvilWP = new WorldPoint(3188,3427,0);
    public WorldPoint bankWP = new WorldPoint(3185,3436,0);
    public static int barsSmelted = 0;
    public static int barPrice= 0;
    public static int primOrePrice = 0;
    public static int secOrePrice = 0;
    public static int stamPotSipPrice = 0;
    public static int cofferAndForemanAndSipsSpent = 0;
    public static boolean masterOnSwitch = true;
    public static blastFurnanceStates currentState = blastFurnanceStates.state_addFriend;
    public static String primOre;
    public static String secOre;
    public static String barName;
    public static int coalAmountPerPrim;
    public int muleHostWorld = 0;
    public int blastFurnaceWorld = 0;

    int primOre_inBank;
    int secOre_inBank;
    int stamPot_inBank;
    int ironBar_inBank;
    int varrockTele_inBank;
    int houseTele_inBank;
    int coins_inBank;

    public void scriptInit(DDBlastFurnaceConfig config) {
        barPrice = Microbot.getItemManager().getItemPriceWithSource(config.BlastFurnaceBarSelection().getBarId(), true);
        primOrePrice = Microbot.getItemManager().getItemPriceWithSource(config.BlastFurnaceBarSelection().getPrimaryId(), true);
        secOrePrice = Microbot.getItemManager().getItemPriceWithSource(config.BlastFurnaceBarSelection().getSecondaryId(), true) * config.BlastFurnaceBarSelection().getCoalRequired();
        stamPotSipPrice = Microbot.getItemManager().getItemPriceWithSource(stamPot1DoseId, true);
        primOre = config.BlastFurnaceBarSelection().getPrimaryOre();
        secOre = config.BlastFurnaceBarSelection().getSecondaryOre();
        coalAmountPerPrim = config.BlastFurnaceBarSelection().getCoalRequired();
        barName = config.BlastFurnaceBarSelection().getName();
        Microbot.getClient().setCameraPitchTarget(383);
    }

    public boolean run(DDBlastFurnaceConfig config) {
        Microbot.enableAutoRunOn = false;
        timeStart = System.currentTimeMillis();
        barsSmelted = 0;
        cofferAndForemanAndSipsSpent = 0;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            //if(!masterOnSwitch) return;
            try {
                //Make sure run is always on as long as we have 20 run energy
                if(Microbot.getClient().getEnergy() > 20 && Rs2Player.isMoving()){
                    Rs2Player.toggleRunEnergy(true);
                }
                Microbot.getClient().setCameraPitchTarget(383);

                switch (currentState) {
                    case state_addFriend:
                        if(addFriend(config)) {
                            if(Rs2Walker.isInArea(dispenserWP, 30)) {
                                currentState = blastFurnanceStates.state_BF_init;
                            }else{
                                currentState = blastFurnanceStates.state_goToGe;
                            }
                        }
                        break;
                    case state_BF_init:
                        if (!Rs2Walker.isInArea(dispenserWP, 30)) {
                            Rs2Walker.walkTo(dispenserWP);
                            sleepUntilTrue(() -> !Rs2Player.isMoving(), 100, 5000);
                            break;
                        }
                        currentState = blastFurnanceStates.state_BF_doBank;
                        break;
                    case state_BF_doBank:
                        doBank(config);
                        if (currentState == blastFurnanceStates.state_goToGe) {
                            break; //We just teleported to varrock
                        }
                        if (Rs2Inventory.get("Coins") != null) {
                            currentState = blastFurnanceStates.state_BF_payForemanAndCoffer;
                        } else if (Rs2Inventory.get("bar") != null) {
                            break;
                        } else {
                            currentState = blastFurnanceStates.state_BF_placeOreToConveyer;
                        }
                        break;
                    case state_BF_payForemanAndCoffer:
                        payForemanAndCoffer(config);
                        currentState = blastFurnanceStates.state_BF_doBank;
                        break;
                    case state_BF_placeOreToConveyer:
                        if ((Rs2Inventory.get(config.BlastFurnaceBarSelection().getPrimaryOre()) != null) ||
                                (Rs2Inventory.get(coalId) != null)) {
                            if (config.useIceGlove()) {
                                //We are using ice glove, Go to conveyor
                                placeOreOntoConveyer();
                            } else {
                                //We are not using ice glove
                                if (Rs2Inventory.contains(bucketId)) {
                                    fillBucket();
                                    placeOreOntoConveyer();
                                } else if (Rs2Inventory.contains(bucketOfWaterId)) {
                                    placeOreOntoConveyer();
                                }
                            }
                            currentState = blastFurnanceStates.state_BF_takeFromDispenser;
                        } else {
                            currentState = blastFurnanceStates.state_BF_doBank;
                        }
                        break;
                    case state_BF_takeFromDispenser:
                        if (!skipTakeBar && placeConveyerCnt == 2) {
                            if (Math.random() > 0.25) {
                                Rs2Walker.walkMiniMap(dispenserWP);
                            }
                            sleepUntil(this::dispenserHasBar);
                            grabBarsFromDispenser(config.useIceGlove(), config.BlastFurnaceBarSelection().getVarbit());

                        }
                        //Corner case where we didnt get the bar for some odd reason
                        if (dispenserHasBar() && !Rs2Inventory.isFull()) {
                            grabBarsFromDispenser(config.useIceGlove(), config.BlastFurnaceBarSelection().getVarbit());
                        }
                        placeConveyerCnt = 0;
                        currentState = blastFurnanceStates.state_BF_doBank;
                        break;
                    case state_goToGe:
                        if (!Rs2Walker.isInArea(BankLocation.GRAND_EXCHANGE.getWorldPoint(), 5)) {
                            if (!Rs2Player.isMoving()) {
                                Rs2Walker.restartPathfinding(Rs2Player.getWorldLocation(), new WorldPoint(3161, 3490, 0));
                            }
                            Rs2Walker.walkTo(new WorldPoint(3161, 3490, 0));
                            break;
                        } else {
                            sleepUntilTrue(() -> !Rs2Player.isMoving(), 600, 5000);
                            currentState = blastFurnanceStates.state_sellProducts;
                        }
                        break;
                    case state_sellProducts:
                        if (Rs2Inventory.isEmpty()) {
                            withdrawAllAsset(config);
                            if (Rs2Inventory.isEmpty()) {
                                currentState = blastFurnanceStates.state_buySupplies;
                                break;
                            }
                        }
                        if (Rs2GrandExchange.openExchange()) {
                            if (Rs2GrandExchange.sellInventory()) {
                                //Everything from inventory is placed onto market
                                //sleepUntilTrue(Rs2GrandExchange::hasSoldOffer, 100, 10000);
                                sleep(5000, 10000);
                                if (Rs2GrandExchange.hasSoldOffer()) {
                                    Rs2GrandExchange.collectToBank();
                                }
                                currentState = blastFurnanceStates.state_buySupplies;
                            }
                        }
                        break;
                    case state_buySupplies:
                        if (updateInBankCount(config)) {
                            if (buySupplies(config)) {
                                Rs2GrandExchange.closeExchange();
                                currentState = blastFurnanceStates.state_decideScriptToRun;
                            }
                        }
                        break;
                    case state_decideScriptToRun:
                        updateInBankCount(config); //Get Bank item counts agian
                        int scriptDecided = decideScript(config);
                        switch (scriptDecided) {
                            case 0:
                                //We dont have supplies for either.
                                Rs2Player.logout();
                                Microbot.pauseAllScripts = true;
                                //currentState = blastFurnanceStates.state_buySupplies;
                                break;
                            case 1: //muling time
                                currentState = blastFurnanceStates.state_muling_goToHouse;
                                break;
                            case 2:
                                currentState = blastFurnanceStates.state_BF_goToKeldagrim;
                                break;
                            case 3:
                                currentState = blastFurnanceStates.state_DM_init;
                                break;
                            default:
                                break;
                        }
                        break;
                    case state_BF_goToKeldagrim:
                        Rs2Walker.walkTo(3141, 3504, 0);
                        sleepUntilTrue(() -> !Rs2Player.isMoving(), 600, 5000);
                        if (Rs2GameObject.get("Trapdoor") != null) {
                            Rs2GameObject.interact(ObjectID.TRAPDOOR_16168, "Travel");
                            sleep(1200, 2400);
                            sleepUntilTrue(() -> !Rs2Player.isMoving(), 500, 5000);
                        } else {
                            sleep(10000);
                            sleepUntilTrue(() -> !Rs2Player.isMoving(), 500, 5000);
                            currentState = blastFurnanceStates.state_BF_init;
                        }
                        break;
                    case state_DM_init:
                        if (Rs2Player.getWorldLocation().distanceTo(bankWP) > 5) {
                            Rs2Walker.walkTo(bankWP);
                        } else {
                            Rs2Bank.openBank();
                            currentState = blastFurnanceStates.state_DM_bank;
                        }
                        break;
                    case state_DM_bank:
                        if (Rs2Bank.isOpen()) {
                            //check to see if all our GE offers are complete
                            if (geIsComplete()) {
                                while (!Rs2Inventory.isEmpty()) {
                                    Rs2Bank.depositAll();
                                }
                                currentState = blastFurnanceStates.state_goToGe;
                                return;
                            }
                            if (Rs2Inventory.get("hammer") == null) {
                                Rs2Bank.withdrawOne("hammer");
                            }
                            Rs2Bank.depositAllExcept("hammer");

                            if (!Rs2Bank.hasBankItem(ItemID.IRON_BAR, 10)) {
                                Rs2Bank.depositAll();
                                currentState = blastFurnanceStates.state_goToGe;
                                break;
                            }
                            Rs2Bank.withdrawAll(ItemID.IRON_BAR);
                            sleepUntilTrue(Rs2Inventory::isFull, 600, 1200);
                            Rs2Bank.closeBank();
                            currentState = blastFurnanceStates.state_DM_makeDarts;
                        } else {
                            Rs2Bank.openBank();
                        }
                        break;
                    case state_DM_makeDarts:

                        if (Microbot.isGainingExp ||
                                Rs2Player.isAnimating() || Rs2Player.isMoving()) {
                            return;
                        }
                        if (Rs2GameObject.get("anvil") == null) {
                            Rs2Walker.walkTo(anvilWP);
                        } else {
                            if (Rs2Inventory.contains(ItemID.IRON_BAR)) {
                                Rs2GameObject.interact("Anvil", "Smith");
                                sleep(600, 1200);
                                sleepUntil(() -> Rs2Widget.hasWidget("What would you like"));
                                Rs2Widget.clickWidget(20447261);
                                sleepUntil(() -> Microbot.isGainingExp);
                            }
                        }
                        if (!Rs2Inventory.contains(ItemID.IRON_BAR)) {
                            if (Rs2GameObject.get("bank") == null) {
                                Rs2Walker.walkTo(bankWP);
                            } else {
                                if (!Rs2Bank.isOpen())
                                    Rs2Bank.openBank();
                            }
                            currentState = blastFurnanceStates.state_DM_bank;
                        }
                        break;
                    case state_muling_goToHouse:
                        Rs2Bank.openBank();
                        if (Rs2Bank.isOpen()) {
                            while(Rs2Bank.hasBankItem("Coins", config.mulingAmt())) {
                                Rs2Bank.withdrawX("Coins", coins_inBank - config.mulingAmt() + 1);
                                sleep(600, 1200);
                            }
                            while (!Rs2Inventory.contains("Teleport to house")) {
                                Rs2Bank.withdrawOne("Teleport to house");
                                sleep(600, 1200);
                            }
                            while (!Rs2Inventory.contains("Varrock teleport")) {
                                Rs2Bank.withdrawOne("Varrock teleport");
                                sleep(600, 1200);
                            }
                            Rs2Bank.closeBank();
                            sleepUntilTrue(() -> !Rs2Bank.isOpen(), 600, 5000);
                            while (Rs2Inventory.contains("Teleport to house")) {
                                Rs2Inventory.interact("Teleport to house", "Outside");
                                sleep(2000);
                                sleepUntilTrue(() -> !Rs2Player.isAnimating(), 100, 5000);
                            }
                            currentState = blastFurnanceStates.state_muling_changeWorld;
                        }
                        break;

                    case state_muling_changeWorld:
                        if (Microbot.getClient().getWorld() != muleHostWorld) {
                            blastFurnaceWorld = Microbot.getClient().getWorld();
                            Microbot.hopToWorld(muleHostWorld);
                        } else {
                            currentState = blastFurnanceStates.state_muling_enterHouse;
                        }
                        break;

                    case state_muling_enterHouse:
                        if(Microbot.getClient().getWorld() != muleHostWorld){
                            //Case where the world is full.
                            currentState = blastFurnanceStates.state_muling_changeWorld;
                            break;
                        }
                        if (Rs2GameObject.exists(POHId)){
                            if (!Rs2Widget.hasWidget("Enter Name")) {
                                Rs2GameObject.interact(POHId, "Friend's House");
                                sleep(600,1200);
                            } else {
                                Rs2Keyboard.typeString(config.mulingHost());
                                sleep(600,2400);
                                Rs2Keyboard.enter();
                                sleep(600,2400);
                            }
                        }else{
                            currentState = blastFurnanceStates.state_muling_depositTip;
                            //Microbot.pauseAllScripts = true;
                        }
                        break;

                    case state_muling_depositTip:
                        if(Rs2GameObject.exists(29146)){
                            if(Rs2Inventory.contains("Coins")){
                                Rs2GameObject.interact("Tip Jar");
                                sleep(600,1200);
                                sleepUntilTrue(()->Rs2Widget.hasWidget("Donate how much?"),100, 5000);
                                Rs2Keyboard.typeString(Integer.toString(Rs2Inventory.get("Coins").quantity));
                                Rs2Keyboard.enter();
                                sleep(600,1200);
                                sleepUntilTrue(()->Rs2Widget.hasWidget("Donate it"),100, 5000);
                                Rs2Widget.clickWidget("Donate it");
                                sleep(600,1200);
                            }
                        }else{
                            currentState = blastFurnanceStates.state_muling_enterHouse;
                        }
                        if(!Rs2Inventory.contains("Coins")){
                            currentState = blastFurnanceStates.state_muling_gotoGe;
                        }
                        break;
                    case state_muling_gotoGe:
                        Rs2Tab.switchToInventoryTab();
                        if(Microbot.getClient().getWorld() != blastFurnaceWorld){
                            //Case where the world is full.
                            Microbot.hopToWorld(blastFurnaceWorld);
                            break;
                        }
                        if(Rs2Inventory.contains("Varrock teleport")){
                            Rs2Inventory.interact("Varrock teleport", "break");
                            sleep(2000);
                            sleepUntilTrue(() -> !Rs2Player.isAnimating(),100,5000);
                        }else{
                            currentState = blastFurnanceStates.state_goToGe;
                        }
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

    int decideScript(DDBlastFurnaceConfig config){
        FriendContainer friendContainer = Microbot.getClient().getFriendContainer();
        Friend hostFriend = friendContainer.findByName(config.mulingHost());
        if(hostFriend != null && hostFriend.getWorld() > 0) {
            if (coins_inBank >= config.mulingAmt()) {
                muleHostWorld = hostFriend.getWorld();
                return 1;
            }
        }
        if(primOre_inBank >= config.blastFurnaceRestockAmount() &&
            secOre_inBank >= (config.blastFurnaceRestockAmount()*config.BlastFurnaceBarSelection().getCoalRequired()) &&
            stamPot_inBank >= (config.blastFurnaceRestockAmount()/200)) {
            return 2;
        }
        if(ironBar_inBank >= (config.dartMakerRestockAmount())){
            return 3;
        }
        return 0;
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
        if(!Rs2Bank.hasBankItem(config.BlastFurnaceBarSelection().getPrimaryId(),30) ||
                !Rs2Bank.hasBankItem(config.BlastFurnaceBarSelection().getSecondaryId(), 30)){//||
                //!Rs2Bank.hasBankItem(coinID, config.cofferReloadAmount())){
            //Microbot.pauseAllScripts = true;
            Rs2Bank.depositAll();
            Rs2Bank.withdrawOne("Varrock teleport");
            sleepUntilTrue(()->Rs2Inventory.contains("Varrock teleport"), 100,2000);
            Rs2Bank.closeBank();
            while(Rs2Inventory.contains("Varrock teleport")){
                Rs2Inventory.interact("Varrock teleport", "break");
                sleep(2000);
                sleepUntilTrue(() -> !Rs2Player.isAnimating(),100,5000);
                currentState = blastFurnanceStates.state_goToGe;
            }
            //masterOnSwitch = false;
            return;
        }
        //We have to deposit everything including bars. Leave coal bag and bucket to start new.
        Rs2Bank.depositAllExcept("Coal bag","bucket","bucket of water");
        //Check if we need to pay coffer or foreman
        int amountWithdraw = needToPayCoffer(config.cofferMinimum(),config.cofferReloadAmount()) + needToPayForeman();
        if(amountWithdraw > 0){
            cofferAndForemanAndSipsSpent += amountWithdraw;
            Rs2Bank.withdrawX("Coins", amountWithdraw);
            sleepUntilTrue(() -> Rs2Inventory.contains("Coins"), 100, 5000);
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
                sleepUntil(()-> Rs2Inventory.contains("Stamina potion"));
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
            Rs2Keyboard.typeString(Integer.toString(Rs2Inventory.get("Coins").quantity));
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

    void withdrawAllAsset(DDBlastFurnaceConfig config){
        if(!Rs2Bank.isOpen()){
            Rs2Bank.openBank();
        }
        Rs2Bank.depositAll();
        Rs2Bank.setWithdrawAsNote();
        sleep(600,1200);
        //For blast furnace assets
        if(Rs2Bank.hasBankItem(config.BlastFurnaceBarSelection().getName())) {
            Rs2Bank.withdrawAll(config.BlastFurnaceBarSelection().getBarId());
            sleep(600, 1200);
        }
        //For Dart Making assets
        if(Rs2Bank.hasBankItem("Iron dart tip")) {
            Rs2Bank.withdrawAll("Iron dart tip");
            sleep(600, 1200);
        }
        Rs2Bank.closeBank();
        sleepUntilTrue(()->!Rs2Bank.isOpen(),100,5000);
    }

    boolean updateInBankCount(DDBlastFurnaceConfig config){
        if(!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntilTrue(Rs2Bank::isOpen, 600, 5000);
        }
        sleep(600,1200);
        if(Rs2Bank.hasItem(config.BlastFurnaceBarSelection().getPrimaryId())) {
            primOre_inBank = Rs2Bank.findBankItem(config.BlastFurnaceBarSelection().getPrimaryOre()).quantity;
        }else{
            primOre_inBank = 0;
        }
        if(Rs2Bank.hasItem(config.BlastFurnaceBarSelection().getSecondaryId())) {
            secOre_inBank = Rs2Bank.findBankItem(config.BlastFurnaceBarSelection().getSecondaryOre()).quantity;
        }else{
            secOre_inBank = 0;
        }
        if(Rs2Bank.hasItem("Stamina potion(4)")) {
            stamPot_inBank = Rs2Bank.findBankItem("Stamina potion(4)").quantity;
        }else {
            stamPot_inBank = 0;
        }
        if(Rs2Bank.hasItem("Iron bar")) {
            ironBar_inBank = Rs2Bank.findBankItem("Iron bar").quantity;
        }else {
            ironBar_inBank = 0;
        }
        if(Rs2Bank.hasItem("Varrock teleport")) {
            varrockTele_inBank = Rs2Bank.findBankItem("Varrock teleport").quantity;
        }else {
            varrockTele_inBank = 0;
        }
        if(Rs2Bank.hasItem("Teleport to house")) {
            houseTele_inBank = Rs2Bank.findBankItem("Teleport to house").quantity;
        }else {
            houseTele_inBank = 0;
        }
        if(Rs2Bank.hasItem("Coins")) {
            coins_inBank = Rs2Bank.findBankItem("Coins").quantity;
        }else {
            coins_inBank = 0;
        }
        Rs2Bank.closeBank();
        sleepUntilTrue(()->!Rs2Bank.isOpen(),100, 5000);
        return true;
    }

    boolean buySupplies(DDBlastFurnaceConfig config){
        if(!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
        }
        if(primOre_inBank < config.blastFurnaceRestockAmount() && !itemAlreadyTradingInGE(config.BlastFurnaceBarSelection().getPrimaryId())) {
            Rs2GrandExchange.buyItemGePrice(config.BlastFurnaceBarSelection().getPrimaryOre(), config.blastFurnaceRestockAmount() - primOre_inBank);
        }
        if(secOre_inBank < (config.blastFurnaceRestockAmount()*config.BlastFurnaceBarSelection().getCoalRequired()) && !itemAlreadyTradingInGE(ItemID.COAL)){
            Rs2GrandExchange.buyItemGePrice(config.BlastFurnaceBarSelection().getSecondaryOre(), (config.blastFurnaceRestockAmount()*config.BlastFurnaceBarSelection().getCoalRequired()) - secOre_inBank);
        }
        if(stamPot_inBank < (config.blastFurnaceRestockAmount()/200)  && !itemAlreadyTradingInGE(ItemID.STAMINA_POTION4)){
            Rs2GrandExchange.buyItemGePrice("Stamina potion(4)", (config.blastFurnaceRestockAmount()/200) - stamPot_inBank);
        }
        if(ironBar_inBank < (config.dartMakerRestockAmount()) && !itemAlreadyTradingInGE(ItemID.IRON_BAR)){
            Rs2GrandExchange.buyItemGePrice("Iron bar", config.dartMakerRestockAmount() - ironBar_inBank);
        }
        if(varrockTele_inBank < 10  && !itemAlreadyTradingInGE(ItemID.VARROCK_TELEPORT)){
            Rs2GrandExchange.buyItemGePrice("Varrock teleport", 10);
        }
        if(houseTele_inBank < 10  && !itemAlreadyTradingInGE(ItemID.TELEPORT_TO_HOUSE)){
            Rs2GrandExchange.buyItemGePrice("Teleport to house", 10);
        }
        sleepUntilTrue(Rs2GrandExchange::hasBoughtOffer, 100, 5000);
        Rs2GrandExchange.collect(true);
        return true;
    }

    boolean geIsComplete(){
        GrandExchangeOffer[] offers = Microbot.getClient().getGrandExchangeOffers();
        for (GrandExchangeOffer offer : offers){
            if(offer.getState() == GrandExchangeOfferState.BUYING)
                return false;
        }
        return true;
    }

    boolean itemAlreadyTradingInGE(int itemId){
        GrandExchangeOffer[] offers = Microbot.getClient().getGrandExchangeOffers();
        for (GrandExchangeOffer offer : offers){
            if(offer.getItemId() == itemId)
                return true;
        }
        return false;
    }

    boolean addFriend(DDBlastFurnaceConfig config){
        //Check if muling host is in our friends list to see if hes online when we need to drop
        if(Rs2Tab.getCurrentTab() == InterfaceTab.FRIENDS) {
            if (!Microbot.getClient().isFriended(config.mulingHost(), false)) {
                //Add the friend
                Widget addFriend = Rs2Widget.getWidget(28114959);
                if(addFriend.getText().equalsIgnoreCase("Add Friend")){
                    Microbot.getMouse().click(addFriend.getBounds());
                    sleep(600, 1200);
                    sleepUntilTrue(() -> Rs2Widget.hasWidget("Enter name of friend to add to list"), 250, 5000);
                }else{
                    return false;
                }
                if (Rs2Widget.hasWidget("Enter name of friend to add to list")) {
                    Rs2Keyboard.typeString(config.mulingHost());
                    Rs2Keyboard.enter();
                }
                Rs2Tab.switchToInventoryTab();
                return true;
            }else {
                Rs2Tab.switchToInventoryTab();
                return true;
            }
        }else{
            Rs2Tab.switchToFriendsTab();
            return false;
        }
    }

    @Override
    public void shutdown() {
        currentState = blastFurnanceStates.state_addFriend;
        super.shutdown();
    }
}

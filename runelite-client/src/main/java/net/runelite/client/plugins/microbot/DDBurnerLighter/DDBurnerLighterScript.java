package net.runelite.client.plugins.microbot.DDBurnerLighter;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.events.ChatInput;
import net.runelite.client.events.ChatboxInput;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.WidgetIndices;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


enum states{
    state_init,
    state_walkToHousePortal,
    state_atPortal,
    state_atHouse,
    state_atPhails
}
public class DDBurnerLighterScript extends Script {
    //For overlay
    public static double version = 1.0;
    public static String host1;
    public static String host2;
    public static String comment;
    public static int userOffline = 0;

    public static int incenseBurnDurationTick;
    public int randNum;
    public static long[] lightStartTimestamp = new long[]{0,0};
    public int hostNumber = 0;
    String hostName;
    //ITEM IDS
    public static int notedMarentillId = 252;
    public static int marentillId = 251;
    public static int tinderboxId = 590;
    public static int coinId = 995;
    public static int POHId = 15478;
    public static int POHExitId = 4525;
    public static int burnerId = 13213;
    public static int unlitBurnerId = 13212;
    public static int alterId = 13197;

    //WORLD POINTS
    WorldPoint POHWorldPoint = new WorldPoint(2953, 3223, 0);
    WorldPoint PhialsWorldPoint = new WorldPoint(2951, 3216, 0);
    public int POHWorldPointSize = 5;
    public static boolean fullyInit = false;
    //Script states
    public static states currentState = states.state_init;

    public boolean run(DDBurnerLighterConfig config) {
        Microbot.enableAutoRunOn = false;
        host1 = config.message1();
        host2 = config.message2();
        incenseBurnDurationTick = 150 + Microbot.getClient().getRealSkillLevel(Skill.FIREMAKING) - randNum;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                //TODO:Check to make sure that the account has at least 35FM or else return
                //Make sure we not going nuts when the player is walking
                while (Rs2Player.isWalking()) {
                    sleep(100);
                }
                switch (currentState) {
                    case state_init:
                        comment = "Initializing Script";
                        //make sure we are on world 330
                        if (Microbot.getClient().getWorld() != 330) {
                            Microbot.showMessage("Player not in world 330");
                            //Microbot.hopToWorld(330);
                            //sleep(10000); //Let it sleep for 10 sec
                        }
                        //Change the minimap zoom and make top down camera
                        Microbot.getClient().setMinimapZoom(2.0);
                        Microbot.getClient().setScalingFactor(272);
                        Microbot.getMouse().scrollDown(new Point(Rs2Player.getLocalLocation().getX(), Rs2Player.getLocalLocation().getY()));
                        Rs2Camera.setPitch(100);
                        //Open up inventory
                        if (!Rs2Inventory.isOpen())
                            Rs2Inventory.open();

                        //check to make sure we have all item and choose next state
                        if (!Rs2Inventory.contains(notedMarentillId) || !Rs2Inventory.contains("Coins") || !Rs2Inventory.contains(tinderboxId)) {
                            Microbot.showMessage("Inventory Missing Item");
                        } else {
                            //All guest houses are plane 1
                            if (!isInHouse()) {
                                currentState = states.state_walkToHousePortal;
                            } else {
                                currentState = states.state_atHouse;
                            }
                        }
                        break;
                    case state_walkToHousePortal:
                        if (isInHouse()) {
                            currentState = states.state_atHouse;
                            break;
                        }

                        //Walk to the portal if we are not currently at the portal
                        if (Rs2Player.getWorldLocation().distanceTo2D(POHWorldPoint) > POHWorldPointSize) {
                            //Not inside the area for advertising, Walk to advertising place
                            if (!Rs2Player.isWalking()) {
                                Rs2Walker.walkTo(POHWorldPoint, 0);
                                comment = "Walking to POH";
                            } else {
                                if (Microbot.getClient().getEnergy() > 20) {
                                    Rs2Player.toggleRunEnergy(true);
                                }
                            }
                        } else {//We are currently at the portal, go to state to see if we go into house
                            currentState = states.state_atPortal;
                        }
                        break;
                    case state_atPortal:
                        //Make sure we are at the portal area in this state or we go back
                        if (Rs2Player.getWorldLocation().distanceTo2D(POHWorldPoint) > POHWorldPointSize) {
                            currentState = states.state_walkToHousePortal;
                            break;
                        }
                        comment = "Currently at portal";
                        //Check if we have more than 2 maretill in our inventory
                        if (Rs2Inventory.count(marentillId) >= 2) {
                            //We have enough, lets go into a portal
                            //Check which host we have to go to
                            enterPortal(config, choosenHost(config));
                            currentState = states.state_atHouse;
                        } else {
                            //We dont have enough herbs, go unnote
                            Rs2Walker.walkTo(PhialsWorldPoint, 0);
                            currentState = states.state_atPhails;
                        }
                        break;
                    case state_atPhails:
                        unoteMarentill();
                        if (!Rs2GameObject.exists(POHId)) {
                            comment = "Walking to POH";
                            Rs2Walker.walkFastCanvas(POHWorldPoint);
                        }
                        currentState = states.state_atPortal;
                        break;

                    case state_atHouse:
                        if (!isInHouse()) {
                            currentState = states.state_walkToHousePortal;
                            //User is most likely offline
                            userOffline |= (1 << hostNumber);
                            System.out.println("offline: " + userOffline);
                            if(userOffline == 3){
                                Microbot.pauseAllScripts = true;
                                return;
                            }
                            break;
                        }
                        if (Rs2Inventory.count(marentillId) >= 2) {
                            if ((lightStartTimestamp[hostNumber] == 0)
                                    || ((Microbot.getClient().getTickCount() - lightStartTimestamp[hostNumber]) >= (incenseBurnDurationTick))) {
                                lightStartTimestamp[hostNumber] = Microbot.getClient().getTickCount();
                                lightBurners();
                                if(config.useSecondHost()){
                                    exitHousePortal();
                                    currentState = states.state_atPortal;
                                }else{
                                    walkToAltar();
                                    currentState = states.state_atHouse;
                                }
                                break;
                            }
                            //Only run random antiban half the time
                            comment = "Afking in " + hostName + "'s House";
                            if(Math.random() < 0.02){
                                runAntiban();
                            }
                        } else {
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
        }, 1000, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    private void unoteMarentill() {
        //Click on the noted maretill in inventory
        comment = "Unnoting with Phails";
        Rs2Inventory.interact(notedMarentillId, "Use");
        sleep(250, 1000);
        Rs2Npc.interact(NpcID.PHIALS, "Use");
        sleep(250, 1000);
        sleepUntil(() -> Rs2Widget.hasWidget("Exchange All"));
        sleep(250, 1000);
        Rs2Widget.clickWidget("Exchange All");
    }

    private boolean enterPortal(DDBurnerLighterConfig config, int hostNumber) {
        comment = "Entering " + hostName + "'s House";
        Rs2GameObject.interact(POHId, "Friend's House");
        sleep(250, 1000);
        sleepUntil(() -> Rs2Widget.hasWidget("Enter Name"));
        sleep(250, 1000);
        if(hostNumber == 0) {
            Rs2Keyboard.typeString(config.message1());
        }else if(hostNumber == 1){
            Rs2Keyboard.typeString(config.message2());
        }
        Rs2Keyboard.enter();
        //Check if the player is online from the chatbox
        //if(Arrays.stream(Microbot.getClient().getChatLineMap().get() == "That player is offline, or has privacy mode enabled") {
        //    sleep(1000);
        //}
        sleepUntil(() -> Rs2Player.getWorldLocation().getPlane() != 0);
        //Choose a new random number for this house
        randNum = (int)(Math.random()*10) *2;
        return true;
    }

    public int choosenHost(DDBurnerLighterConfig config) {
        if (config.useSecondHost()) { //Have 2 Host
            if(userOffline != 0){ //1 of them is offline
                if((userOffline & 0x1) == 0x1){
                    hostNumber = 1;
                    hostName = config.message2();
                    return 1;
                }else if((userOffline & 0x2) == 0x2){
                    hostNumber = 0;
                    hostName = config.message1();
                    return 0;
                }
            }
            if (lightStartTimestamp[0] > lightStartTimestamp[1]) {
                hostNumber = 1;
                hostName = config.message2();
                return 1;
            } else {
                hostNumber = 0;
                hostName = config.message1();
                return 0;
            }
        } else { //Only single host
            hostNumber = 0;
            hostName = config.message1();
            return 0;
        }
    }

    private void exitHousePortal() {
        comment = "Leaving house";
        userOffline = 0;//Give both host a chance in 2 host config
        Rs2Camera.turnTo(Rs2GameObject.findObjectById(POHExitId));
        Rs2GameObject.interact("Portal", "Enter");
        sleepUntil(() -> Rs2Player.getWorldLocation().getPlane() == 0);
    }

    private boolean isInHouse() {
        return Rs2GameObject.exists(POHExitId) || Rs2GameObject.exists(13179) || Rs2GameObject.exists(40878)
                || Rs2GameObject.exists(burnerId) || Rs2GameObject.exists(unlitBurnerId);
    }

    private void lightBurners() {
        comment = "Lighting burners";
        List<GameObject> burners = Rs2GameObject.getGameObjects()
                .stream()
                .filter(x -> x.getId() == burnerId || x.getId() == unlitBurnerId)
                .sorted(Comparator.comparingInt(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation())))
                .collect(Collectors.toList());

        if (burners.get(0) != null) {
            Rs2GameObject.interact(burners.get(0), "Light");
            sleep(2500, 3000);
            sleepUntil(() -> !Rs2Player.isWalking() && !Rs2Player.isAnimating());
        }

        if (burners.get(1) != null) {
            Rs2GameObject.interact(burners.get(1), "Light");
            sleep(2500, 3000);
            sleepUntil(() -> !Rs2Player.isWalking() && !Rs2Player.isAnimating());
        }
    }
    private void walkToAltar(){
        comment = "Walking to Altar";
        WorldPoint alterWP;
        GameObject gildedAlter = Rs2GameObject.getGameObjects()
                .stream()
                .filter(x -> x.getId() == 13179 || x.getId() == 40878)
                .sorted(Comparator.comparingInt(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation())))
                .filter(Rs2GameObject::hasLineOfSight)
                .findFirst()
                .orElse(null);
        Rs2GameObject.interact(gildedAlter, "Pray");
    }
    private void runAntiban(){
        int Min = 1;
        int Max = 5;
        int randNum = Min + (int)(Math.random() * ((Max - Min) + 1));
        System.out.println("Antiban: " + randNum);
        switch (randNum){
            case 1:
                GameObject poolOfRejuvenation = Rs2GameObject.getGameObjects()
                    .stream()
                    .filter(x -> x.getId() == Rs2GameObject.get("Rejuvenation").getId())
                    .sorted(Comparator.comparingInt(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation())))
                    .filter(Rs2GameObject::hasLineOfSight)
                    .findFirst()
                    .orElse(null);
                if (poolOfRejuvenation != null){
                    Rs2GameObject.interact(poolOfRejuvenation);
                }
                break;
            case 2:
                Rs2GameObject.interact("Altar", "Pray");
                break;
            case 3:
                Rs2Walker.walkFastCanvas(Rs2GameObject.get("Altar").getWorldLocation());
                break;
            case 4:
                Microbot.getMouse().move(Rs2Player.getWorldLocation().getX() + 3,Rs2Player.getWorldLocation().getY()+ 3);
                break;
            case 5:
                exitHousePortal();
            default:
                break;
        }
    }
}

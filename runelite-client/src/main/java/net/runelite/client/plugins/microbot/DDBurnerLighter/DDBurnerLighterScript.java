package net.runelite.client.plugins.microbot.DDBurnerLighter;

import com.google.inject.internal.util.Classes;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.ChatMessageType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
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

import javax.inject.Inject;
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
    public static double version = 2.0;
    public static String host1;
    public static String host2;
    public static String comment;

    //For making sure account not offline
    public int userOffline = 0;

    public static int incenseBurnDurationTick;
    public int randNum;
    public static long[] lightStartTimestamp = new long[]{0, 0};
    public static WorldPoint[] alterWP = new WorldPoint[]{null, null};
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
        incenseBurnDurationTick = 100 + Microbot.getClient().getRealSkillLevel(Skill.FIREMAKING) - randNum;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (!fullyInit) {
                    comment = "Initializing Script";
                    if (Microbot.getClient().getWorld() != 330) {
                        Rs2Keyboard.typeString("::hop 330");
                        Rs2Keyboard.enter();
                        sleep(10000); //Let it sleep for 10 sec
                    }
                    //Change the minimap zoom and make top down camera
                    Microbot.getClient().setMinimapZoom(2.0);
                    Microbot.getClient().setScalingFactor(272);
                    //while(Microbot.getClient().get >= 300)
                    Microbot.getMouse().scrollDown(new Point(Rs2Player.getLocalLocation().getX(), Rs2Player.getLocalLocation().getY()));
                    Rs2Camera.setPitch(100);
                    //Open up inventory
                    if (!Rs2Inventory.isOpen())
                        Rs2Inventory.open();

                    //check to make sure we have all item and choose next state
                    if (!Rs2Inventory.contains(notedMarentillId) || !Rs2Inventory.contains("Coins") || !Rs2Inventory.contains(tinderboxId)) {
                        Microbot.showMessage("Inventory Missing Item");
                    }
                    fullyInit = true;
                }
                //Always have run on!
                if (Microbot.getClient().getEnergy() > 20) {
                    Rs2Player.toggleRunEnergy(true);
                }
                //This is main loop
                if (!hasMarentill()) {
                    unoteMarentill();
                } else {
                    ///Check if we are in house
                    if (isInHouse()) {
                        //check if we are using 2 host
                        if (isTimeToLightBurner()) {
                            lightBurners();
                            if(config.useSecondHost()) { //Dont exit if we are only using 1 because stupid to exit
                                exitHousePortal();
                            }else{
                                walkToAltar(); //We are going to just walk to alter after lighjting
                            }
                        } else {
                            //if we dont have enough time to run antiban dont run it
                            //if(((Microbot.getClient().getTickCount() - lightStartTimestamp[hostNumber]) < (incenseBurnDurationTick- 20)))
                            //    runAntiban();
                            comment = "Afking in " + hostName + "'s House";
                        }
                    } else { //not inside house
                        //See if we can see the portal
                        if (!Rs2GameObject.exists(POHId)) {
                            comment = "Walking to POH";
                            Rs2Walker.walkFastCanvas(POHWorldPoint);
                        } else {
                            //The portal is in view, figure out which house to get in
                            enterPortal(config, choosenHost(config));
                        }
                    }
                }
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

    private boolean isTimeToLightBurner() {
        return ((lightStartTimestamp[hostNumber] == 0)
                || ((Microbot.getClient().getTickCount() - lightStartTimestamp[hostNumber]) >= (incenseBurnDurationTick)));
    }

    private void unoteMarentill() {
        //is inside a house or not?
        if (!isInHouse()) {
            if (Rs2Player.getWorldLocation().distanceTo2D(PhialsWorldPoint) > 5) {
                Rs2Walker.walkTo(PhialsWorldPoint);
                comment = "Walking to Phails";
            } else {
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
        } else { //we are inside of the house so we have to gtfo first
            Rs2GameObject.interact(POHExitId, "Enter");
            Rs2Walker.walkTo(PhialsWorldPoint);
            comment = "Walking to Phails";
        }
    }

    private boolean enterPortal(DDBurnerLighterConfig config, int hostNumber) {
        comment = "Entering " + hostName + "'s House";
        Rs2GameObject.interact(POHId, "Friend's House");
        sleep(250, 1000);
        sleepUntil(() -> Rs2Widget.hasWidget("Enter Name"));
        sleep(250, 1000);
        if (hostNumber == 0) {
            Rs2Keyboard.typeString(config.message1());
        } else if (hostNumber == 1) {
            Rs2Keyboard.typeString(config.message2());
        }
        Rs2Keyboard.enter();
        sleep(1000, 2000);
        sleepUntil(this::isInHouse, 1000);
        //Choose a new random number for this house
        randNum = (int) (Math.random() * 10) * 2;
        return true;
    }

    public int choosenHost(DDBurnerLighterConfig config) {
        if (config.useSecondHost()) {
            if (userOffline == 1 || userOffline == 2) { //1 of them is offline
                if ((userOffline & 0x1) == 0x1) {
                    hostNumber = 1;
                    hostName = config.message2();
                    return 1;
                } else if ((userOffline & 0x2) == 0x2) {
                    hostNumber = 0;
                    hostName = config.message1();
                    return 0;
                }
            } else if (userOffline == 3) {
                Microbot.pauseAllScripts = true;
                return 99;
            } else { //both host is online. We will look which has a lower timestamp
                if (lightStartTimestamp[0] <= lightStartTimestamp[1]) { //Less than start means further away
                    hostNumber = 0;
                    hostName = config.message1();
                    return 0;
                } else {
                    hostNumber = 1;
                    hostName = config.message2();
                    return 1;
                }
            }
        } else { //no second host. Return 0
            hostNumber = 0;
            hostName = config.message1();
            return 0;
        }
        return 99;
    }

    private void exitHousePortal() {
        comment = "Leaving house";
        userOffline = 0;//Give both host a chance in 2 host config
        Rs2Camera.turnTo(Rs2GameObject.findObjectById(POHExitId));
        Rs2GameObject.interact(POHExitId, "Enter");
        sleepUntil(() -> Rs2Player.getWorldLocation().getPlane() == 0);
    }

    private boolean isInHouse() {
        return Rs2GameObject.exists(POHExitId) || Rs2GameObject.exists(13179) || Rs2GameObject.exists(40878)
                || Rs2GameObject.exists(burnerId) || Rs2GameObject.exists(unlitBurnerId);
    }

    private void lightBurners() {
        comment = "Lighting burners";
        lightStartTimestamp[hostNumber] = Microbot.getClient().getTickCount();
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

    private void walkToAltar() {
        comment = "Walking to Altar";
        if(alterWP[hostNumber] == null) {
            System.out.println("Looking for AltarWP " + hostNumber);
            GameObject gildedAlter = Rs2GameObject.getGameObjects()
                    .stream()
                    .filter(x -> x.getId() == Rs2GameObject.get("Altar").getId())
                    .sorted(Comparator.comparingInt(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation())))
                    .filter(Rs2GameObject::hasLineOfSight)
                    .findFirst()
                    .orElse(null);
            if(gildedAlter != null) {
                alterWP[hostNumber] = new WorldPoint(gildedAlter.getWorldLocation().getX() + 1, gildedAlter.getWorldLocation().getY(), gildedAlter.getWorldLocation().getPlane());
                Rs2Walker.walkCanvas(alterWP[hostNumber]);
            }
        }else{
            Rs2Walker.walkCanvas(alterWP[hostNumber]);
        }
    }

    private void runAntiban() {
        int Min = 1;
        int Max = 5;
        if (Math.random() > 0.01) return;
        comment = "Running Antiban";
        int randNum = Min + (int) (Math.random() * ((Max - Min) + 1));
        System.out.println("Antiban: " + randNum);
        switch (randNum) {
            case 1:
                GameObject poolOfRejuvenation = Rs2GameObject.getGameObjects()
                        .stream()
                        .filter(x -> x.getId() == Rs2GameObject.get("Rejuvenation").getId())
                        .sorted(Comparator.comparingInt(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation())))
                        .filter(Rs2GameObject::hasLineOfSight)
                        .findFirst()
                        .orElse(null);
                if (poolOfRejuvenation != null) {
                    Rs2GameObject.interact(poolOfRejuvenation);
                }
                break;
            case 3:
                Microbot.getMouse().move(Rs2Player.getWorldLocation().getX() + 3, Rs2Player.getWorldLocation().getY() + 3);
                break;
            case 4:
                exitHousePortal();
                //Rs2Keyboard.typeString("Open Alter at " + hostName+ "'s House!");
                //Rs2Keyboard.enter();
            default:
                break;
        }
    }

    private boolean hasMarentill() {
        return Rs2Inventory.count(marentillId) >= 2;
    }

}


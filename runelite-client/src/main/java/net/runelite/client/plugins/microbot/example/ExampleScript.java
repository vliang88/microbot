package net.runelite.client.plugins.microbot.example;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.grandexchange.GrandExchangeClient;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.plugins.grandexchange.GrandExchangeSearchMode;
import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnaceConfig;
import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnaceScript;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.agility.models.AgilityObstacleModel;
import net.runelite.client.plugins.microbot.globval.WidgetIndices;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.api.ObjectID.*;
import static net.runelite.client.plugins.microbot.Microbot.log;
import static net.runelite.client.plugins.microbot.util.Global.*;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.getGroundObjects;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class ExampleScript extends Script {
    enum wildRunnerStates{
        state_addFriend,
        state_useObstaclePipe,
        state_useRopeSwing,
        state_useSteppingStone,
        state_useBalanceLog,
        state_useRockClimb,
        state_useDispenser,
        state_getOutPit,
    }

    public static wildRunnerStates currentState = wildRunnerStates.state_addFriend;
    public static wildRunnerStates previousState = wildRunnerStates.state_addFriend;
    public static double version = 1.0;

    public static List<AgilityObstacleModel> wildyCourse = new ArrayList<>();
    int dispenserID = 53224;
    public static boolean dispenserTagged = false;
    public static boolean inPit = false;

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
                Microbot.getClient().setCameraYawTarget(505);

                //Rs2Player.logoutIfPlayerDetected(2);
                int playerThreatInArea = 0;
                List<Player> players = Microbot.getClient().getPlayers();
                for(Player playerInArea: players){
                    if(playerInArea.getCombatLevel() < Microbot.getClient().getLocalPlayer().getCombatLevel() + 50){
                        //Rs2Player.logout();
                        playerThreatInArea++;
                    }
                }
                if(players.size() > 1) {
                    log("Oh Fuck me. " + (players.size() - 1) + "players, " + (playerThreatInArea - 1) + "threat");
                    Rs2Player.logout();
                }


                // Eat food.
                Rs2Player.eatAt(config.hitpoints());

                if (Rs2Player.isMoving()){
                    Rs2Player.eatAt(config.hitpoints());
                    return;
                }
                if (Rs2Player.isAnimating()) return;

                final int agilityExp = Microbot.getClient().getSkillExperience(Skill.AGILITY);
                final int playerHp = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
                switch(currentState){
                    case state_addFriend:
                        if(config.doTraining()){
                            Microbot.getMouse().click(Rs2GameObject.get("Floor spikes").getCanvasLocation());
                            waitForAgilityObstabcleToFinish(agilityExp, playerHp);
                            break;
                        }
                        if(addFriend(config)){
                            currentState = wildRunnerStates.state_useObstaclePipe;
                        }
                        break;
                    case state_useObstaclePipe:
                        GameObject obstaclePipe = Rs2GameObject.getGameObjects()
                                .stream()
                                .filter(x -> x.getId() == OBSTACLE_PIPE_23137)
                                .sorted(Comparator.comparingInt(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation())))
                                .filter(Rs2GameObject::hasLineOfSight)
                                .findFirst()
                                .orElse(null);

                        if(obstaclePipe != null){
                            if(Rs2GameObject.interact(obstaclePipe, "Squeeze-through")) {
                                waitForAgilityObstabcleToFinish(agilityExp, playerHp);
                                currentState = wildRunnerStates.state_useRopeSwing;
                            }
                        }

                        break;
                    case state_useRopeSwing:
                        if(Rs2Player.getWorldLocation().distanceTo2D(new WorldPoint(3005, 3953, 0)) > 8) {
                            Rs2Walker.walkTo(3005, 3953, 0);
                            break;
                        }
                        if(Rs2GameObject.interact(ROPESWING_23132, "Swing-on")) {
                            if(waitForAgilityObstabcleToFinish(agilityExp, playerHp)){
                            //if(Rs2Player.getWorldLocation().getY() > 10000 || Rs2Player.getWorldLocation().getX() > 10000){
                                previousState = wildRunnerStates.state_useRopeSwing;
                                currentState = wildRunnerStates.state_getOutPit;
                                log("Oh fuck we fell");
                                break;
                            }else {
                                currentState = wildRunnerStates.state_useSteppingStone;
                            }
                        }
                        break;
                    case state_getOutPit:
                        if(Rs2Player.getWorldLocation().getY() > 10000 || Rs2Player.getWorldLocation().getX() > 10000) {
                            if(Rs2GameObject.exists(17385)) {
                                Rs2GameObject.interact("Ladder", "Climb-up");
                                sleepUntil(() -> Rs2Player.getWorldLocation().getY() > 10000 && Rs2Player.getWorldLocation().getX() > 10000);
                            }else{
                                Rs2Walker.walkTo(3005, 10362, 0);
                            }
                        }else{
                            currentState = previousState;
                        }
                        break;
                    case state_useSteppingStone:
                        if(Rs2GameObject.interact(STEPPING_STONE_23556, "Cross")) {
                            waitForAgilityObstabcleToFinish(agilityExp, playerHp);
                            if(Rs2Player.getWorldLocation().distanceTo2D(new WorldPoint(2996,3960,0)) > 2){
                                log("Oh fuck we fell" + playerHp + " current: " + Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS));
                                break;
                            }
                            currentState = wildRunnerStates.state_useBalanceLog;
                        }
                        break;
                    case state_useBalanceLog:
                        //if(Rs2Player.getWorldLocation().distanceTo2D(new WorldPoint(3001, 3946, 0)) > 5) {
                        if(!Rs2GameObject.exists(LOG_BALANCE_23542)){
                            Rs2Walker.walkTo(3001, 3946, 0);
                            break;
                        }
                        if(Rs2GameObject.interact(LOG_BALANCE_23542, "Walk-across")) {
                            if(waitForAgilityObstabcleToFinish(agilityExp, playerHp)){
                            //if(Rs2Player.getWorldLocation().getY() > 10000 || Rs2Player.getWorldLocation().getX() > 10000){
                                previousState = wildRunnerStates.state_useSteppingStone;
                                currentState = wildRunnerStates.state_getOutPit;
                                log("Oh fuck we fell");
                                break;
                            }else {
                                currentState = wildRunnerStates.state_useRockClimb;
                            }
                        }
                        break;
                    case state_useRockClimb:
                        if(Rs2GameObject.interact(ROCKS_23640, "Climb")) {
                            waitForAgilityObstabcleToFinish(agilityExp, playerHp);
                            currentState = wildRunnerStates.state_useDispenser;
                        }
                        break;
                    case state_useDispenser:
                        if(Rs2GameObject.interact(dispenserID, "tag")){
                            sleepUntil(() -> dispenserTagged);
                            if(dispenserTagged) {
                                dispenserTagged = false;
                                currentState = wildRunnerStates.state_useObstaclePipe;
                            }
                        }
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
        currentState = wildRunnerStates.state_addFriend;
        super.shutdown();
    }

    private boolean waitForAgilityObstabcleToFinish(final int agilityExp, final int playerHp) {
        sleepUntilOnClientThread(() -> (agilityExp != Microbot.getClient().getSkillExperience(Skill.AGILITY)) ||
                (playerHp >= (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS))+2) ||
                Rs2Player.getWorldLocation().getY() > 10000 ||
                Rs2Player.getWorldLocation().getX() > 10000, 10000);
        return Rs2Player.getWorldLocation().getY() > 10000 ||
                Rs2Player.getWorldLocation().getX() > 10000;
    }

    boolean addFriend(ExampleConfig config){
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
}

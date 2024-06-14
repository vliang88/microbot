package net.runelite.client.plugins.microbot.DDRunedoku;

import net.runelite.api.NPC;
import net.runelite.api.annotations.Component;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;


public class DDRunedokuScript extends Script {
    public static double version = 1.0;
    private int AliID = 3533;
    private int sudokuBoardWidgetID = 19136524;
    private int waterRuneWidgetID = 555;
    private int fireRuneWidgetID = 554;
    public boolean run(DDRunedokuConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                if(Rs2Widget.getWidget(sudokuBoardWidgetID) == null){
                    talkToAli();
                }else{
                    fillSudoku();
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    private void talkToAli(){
        NPC npc = Rs2Npc.getNpc(AliID);
        Rs2Npc.interact(npc, "Trade");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.hasWidget("View runes"));
        Rs2Widget.clickWidget("View runes");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.hasWidget("large casket"));
        Rs2Widget.clickWidget("large casket");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.hasWidget("continue"));
        Rs2Widget.clickWidget("continue");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.hasWidget("Examine lock"));
        Rs2Widget.clickWidget("Examine lock");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.getWidget(sudokuBoardWidgetID) != null);
    }

    private void fillSudoku(){
        Rs2Widget.clickChildWidget(19136521, 1);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

package net.runelite.client.plugins.microbot.magic.aiomagic.scripts;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.magic.aiomagic.AIOMagicPlugin;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.MagicState;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class StunAlchScript extends Script {

    private MagicState state = MagicState.CASTING;
    private final AIOMagicPlugin plugin;

    @Inject
    public StunAlchScript(AIOMagicPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean run() {
        Microbot.enableAutoRunOn = false;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyGeneralBasicSetup();
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2Antiban.setActivity(Activity.TELEPORT_TRAINING);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();


                switch (state) {
                    case CASTING:
                        if (plugin.getAlchItemNames().isEmpty() ||
                                plugin.getAlchItemNames().stream().noneMatch(Rs2Inventory::hasItem)) {
                            Microbot.log("Missing alch items...");
                            return;
                        }

                        if (!Rs2Magic.canCast(MagicAction.HIGH_LEVEL_ALCHEMY) || !Rs2Magic.canCast(MagicAction.LOW_LEVEL_ALCHEMY)) {
                            Microbot.log("Unable to cast alchemy spell");
                            return;
                        }

                        Rs2Item alchItem = null;
                        for (String itemName : plugin.getAlchItemNames()) {
                            if (Rs2Inventory.hasItem(itemName)) {
                                alchItem = Rs2Inventory.get(itemName);
                                break;
                            }
                        }
                        if (alchItem == null) {
                            Microbot.log("Missing alch items...");
                            return;
                        }
                        int inventorySlot = Rs2Player.getRealSkillLevel(Skill.MAGIC) >= 55 ? 11 : 4;
                        if (alchItem.getSlot() != inventorySlot) {
                            Rs2Inventory.moveItemToSlot(alchItem, inventorySlot);
                            return;
                        }

                        net.runelite.api.NPC npc = (net.runelite.api.NPC) Microbot.getClient().getLocalPlayer().getInteracting();

                        if (npc != null) {
                            Rs2Magic.castOn(plugin.getStunSpell().getSpell(), npc);
                        } else {
                            Rs2Magic.castOn(plugin.getStunSpell().getSpell(), Rs2Npc.getNpc(plugin.getStunNpcName()));
                        }

                        if (Rs2AntibanSettings.naturalMouse) {
                            Rs2Magic.alch(alchItem, 10, 50);
                        } else {
                            Rs2Magic.alch(alchItem);
                            sleep(200, 300);
                        }

                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
    }
}

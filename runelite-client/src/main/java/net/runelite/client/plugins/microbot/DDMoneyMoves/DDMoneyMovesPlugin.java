package net.runelite.client.plugins.microbot.DDMoneyMoves;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnaceConfig;
import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnacePlugin;
import net.runelite.client.plugins.microbot.DDBlastFurnace.DDBlastFurnaceScript;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.DD + "DDMoneyMoves",
        description = "DDMoneyMoves plugin",
        tags = {"DDMoneyMoves", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class DDMoneyMovesPlugin extends Plugin {
    @Inject
    private DDMoneyMovesConfig config;
    @Provides
    DDMoneyMovesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DDMoneyMovesConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private net.runelite.client.plugins.microbot.DDMoneyMoves.DDMoneyMovesOverlay DDMoneyMovesOverlay;

    @Inject
    private ClientThread clientThread;

    @Inject
    net.runelite.client.plugins.microbot.DDMoneyMoves.DDMoneyMovesScript DDMoneyMovesScript;

    @Inject
    DDBlastFurnaceScript blastFurnaceScript;

    @Subscribe
    public void onGameTick(GameTick gameTick) {

    }

    @Subscribe
    public void onChatMessage(ChatMessage event){
        
    }

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(DDMoneyMovesOverlay);
        }
        DDBlastFurnaceScript.masterOnSwitch = true;
        DDMoneyMovesScript.run(config);
    }

    protected void shutDown() {
        DDBlastFurnaceScript.masterOnSwitch = false;
        DDMoneyMovesScript.shutdown();
        overlayManager.remove(DDMoneyMovesOverlay);
    }
}

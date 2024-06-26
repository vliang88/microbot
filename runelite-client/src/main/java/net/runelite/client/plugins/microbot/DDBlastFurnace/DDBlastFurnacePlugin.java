package net.runelite.client.plugins.microbot.DDBlastFurnace;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.DD + "DDBlastFurnace",
        description = "DDBlastFurnace plugin",
        tags = {"DDBlastFurnace", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class DDBlastFurnacePlugin extends Plugin {
    @Inject
    private DDBlastFurnaceConfig config;
    @Provides
    DDBlastFurnaceConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DDBlastFurnaceConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DDBlastFurnaceOverlay DDBlastFurnaceOverlay;

    @Inject
    private ClientThread clientThread;

    @Inject
    DDBlastFurnaceScript DDBlastFurnaceScript;

    @Inject


    @Subscribe
    public void onGameTick(GameTick gameTick) {

    }

    @Subscribe
    public void onChatMessage(ChatMessage event){
        if(event.getMessage().contains("coal bag is now empty") ||
                event.getMessage().contains("The coal bag still contains")){
            DDBlastFurnaceScript.coalBagState = 0;
        }
        if(event.getMessage().contains("The coal bag contains ")){
            DDBlastFurnaceScript.coalBagState = 1;
        }
        if(event.getMessage().contains("All your ore goes onto the conveyor belt")){
            DDBlastFurnaceScript.placeConveyerCnt++;
        }
        if(event.getMessage().contains("You should collect your bars before making any more")){
            Microbot.pauseAllScripts = true;
        }
        if(event.getMessage().contains("You don't have anything suitable for putting into the blast furnace")){
            DDBlastFurnaceScript.doBank(config);
        }
    }

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(DDBlastFurnaceOverlay);
        }
        clientThread.invoke(()-> {
            DDBlastFurnaceScript.getPrices(config);
        });
        DDBlastFurnaceScript.run(config);
    }

    protected void shutDown() {
        DDBlastFurnaceScript.shutdown();
        overlayManager.remove(DDBlastFurnaceOverlay);
    }
}

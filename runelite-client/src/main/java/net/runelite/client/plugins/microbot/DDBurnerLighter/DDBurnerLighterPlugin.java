package net.runelite.client.plugins.microbot.DDBurnerLighter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "DDBurnerLighter",
        description = "DDBurnerLighter",
        tags = {"DDBurnerLighter", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class DDBurnerLighterPlugin extends Plugin {
    @Inject
    private DDBurnerLighterConfig config;
    @Provides
    DDBurnerLighterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DDBurnerLighterConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DDBurnerLighterOverlay DDBurnerLighterOverlay;

    @Inject
    DDBurnerLighterScript DDBurnerLighterScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(DDBurnerLighterOverlay);
        }
        DDBurnerLighterScript.run(config);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.ENGINE) {
            if (event.getMessage().equalsIgnoreCase("That player is offline, or has privacy mode enabled.")) {
                DDBurnerLighterScript.userOffline |= (1 << DDBurnerLighterScript.hostNumber);
            }
        }
    }

    protected void shutDown() {
        DDBurnerLighterScript.shutdown();
        overlayManager.remove(DDBurnerLighterOverlay);
    }
}

package net.runelite.client.plugins.microbot.DDRunedoku;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "DDRunedoku",
        description = "DDRunedoku plugin",
        tags = {"DDRunedoku", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class DDRunedokuPlugin extends Plugin {
    @Inject
    private DDRunedokuConfig config;
    @Provides
    DDRunedokuConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DDRunedokuConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DDRunedokuOverlay DDRunedokuOverlay;

    @Inject
    net.runelite.client.plugins.microbot.DDRunedoku.DDRunedokuScript DDRunedokuScript;

    @Subscribe
    public void onGameTick(GameTick event)
    {
            DDRunedokuScript.readyToClick = true;
    }

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(DDRunedokuOverlay);
        }
        DDRunedokuScript.run(config);
    }

    protected void shutDown() {
        DDRunedokuScript.shutdown();
        overlayManager.remove(DDRunedokuOverlay);
    }
}

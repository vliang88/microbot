package net.runelite.client.plugins.microbot.DDAccountBuilder;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "DDAccountBuilder",
        description = "DDAccountBuilder plugin",
        tags = {"DDAccountBuilder", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class DDAccountBuilderPlugin extends Plugin {
    @Inject
    private DDAccountBuilderConfig config;
    @Provides
    DDAccountBuilderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DDAccountBuilderConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DDAccountBuilderOverlay DDAccountBuilderOverlay;

    @Inject
    DDAccountBuilderScript DDAccountBuilderScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(DDAccountBuilderOverlay);
        }
        DDAccountBuilderScript.run(config);
    }

    protected void shutDown() {
        DDAccountBuilderScript.shutdown();
        overlayManager.remove(DDAccountBuilderOverlay);
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
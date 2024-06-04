package net.runelite.client.plugins.microbot.DDBurnerLighter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
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

    protected void shutDown() {
        DDBurnerLighterScript.shutdown();
        overlayManager.remove(DDBurnerLighterOverlay);
    }
}

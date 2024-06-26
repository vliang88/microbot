package net.runelite.client.plugins.microbot.DDAdvertiser;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "DDAdvertiser",
        description = "DDAdvertiser plugin",
        tags = {"DDAdvertiser", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class DDAdvertiserPlugin extends Plugin {
    @Inject
    private DDAdvertiserConfig config;
    @Provides
    DDAdvertiserConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DDAdvertiserConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DDAdvertiserOverlay DDAdvertiserOverlay;

    @Inject
    DDAdvertiserScript DDAdvertiserScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(DDAdvertiserOverlay);
        }
        DDAdvertiserScript.run(config);
    }

    protected void shutDown() {
        DDAdvertiserScript.shutdown();
        overlayManager.remove(DDAdvertiserOverlay);
    }
}

package net.runelite.client.plugins.microbot.DDDartMaker;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.DDDartMaker.DDDartMakerConfig;
import net.runelite.client.plugins.microbot.DDDartMaker.DDDartMakerOverlay;
import net.runelite.client.plugins.microbot.DDDartMaker.DDDartMakerScript;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.DDDartMaker.DDDartMakerScript.lastXpDropMillsecond;

@PluginDescriptor(
        name = PluginDescriptor.DD + "DDDartMaker",
        description = "Microbot DDDartMaker plugin",
        tags = {"DDDartMaker", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class DDDartMakerPlugin extends Plugin {
    @Inject
    private DDDartMakerConfig config;
    @Provides
    DDDartMakerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DDDartMakerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DDDartMakerOverlay DDDartMakerOverlay;

    @Inject
    DDDartMakerScript DDDartMakerScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(DDDartMakerOverlay);
        }
        DDDartMakerScript.run(config);
    }

    protected void shutDown() {
        DDDartMakerScript.shutdown();
        overlayManager.remove(DDDartMakerOverlay);
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
    }
    @Subscribe
    public void onStatChanged(StatChanged event){
        if (event.getSkill() == Skill.SMITHING)
        {
            lastXpDropMillsecond = System.currentTimeMillis();
        }
    }

}

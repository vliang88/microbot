package net.runelite.client.plugins.microbot.sandcrabs;

import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.example.ExampleScript;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.tooltip.Tooltip;

import javax.inject.Inject;
import java.awt.*;

import static java.awt.Color.GREEN;

public class SandCrabOverlay extends OverlayPanel {
    private final SandCrabPlugin plugin;

    @Inject
    SandCrabOverlay(SandCrabPlugin plugin)
    {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Micro SandCrabs Plugin V" + SandCrabScript.version)
                    .color(Color.decode("#a4ffff"))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(plugin.sandCrabScript.state.name())
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("AFK Timer: " + plugin.sandCrabScript.afkTimer)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("HIJACK Timer: " + plugin.sandCrabScript.hijackTimer)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Times Hopped: " + plugin.sandCrabScript.timesHopped)
                    .build());

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}

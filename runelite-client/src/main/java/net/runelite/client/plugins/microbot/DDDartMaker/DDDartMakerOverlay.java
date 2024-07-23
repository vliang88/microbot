package net.runelite.client.plugins.microbot.DDDartMaker;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.DDDartMaker.DDDartMakerPlugin;
import net.runelite.client.plugins.microbot.DDDartMaker.DDDartMakerScript;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class DDDartMakerOverlay extends OverlayPanel {
    @Inject
    DDDartMakerOverlay(DDDartMakerPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Micro DDDartMaker V" + DDDartMakerScript.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}

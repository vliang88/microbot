package net.runelite.client.plugins.microbot.DDMoneyMoves;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class DDMoneyMovesOverlay extends OverlayPanel {
    @Inject
    DDMoneyMovesOverlay(DDMoneyMovesPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {

            panelComponent.setPreferredSize(new Dimension(200, 350));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("DDMoneyMovesScript V:" + DDMoneyMovesScript.version)
                    .color(Color.GREEN)
                    .build());


        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}

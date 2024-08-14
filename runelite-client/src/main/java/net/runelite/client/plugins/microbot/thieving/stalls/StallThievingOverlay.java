<<<<<<<< HEAD:runelite-client/src/main/java/net/runelite/client/plugins/microbot/DDAccountBuilder/DDAccountBuilderOverlay.java
package net.runelite.client.plugins.microbot.DDAccountBuilder;
========
package net.runelite.client.plugins.microbot.thieving.stalls;
>>>>>>>> main:runelite-client/src/main/java/net/runelite/client/plugins/microbot/thieving/stalls/StallThievingOverlay.java

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

<<<<<<<< HEAD:runelite-client/src/main/java/net/runelite/client/plugins/microbot/DDAccountBuilder/DDAccountBuilderOverlay.java
public class DDAccountBuilderOverlay extends OverlayPanel {
    @Inject
    DDAccountBuilderOverlay(DDAccountBuilderPlugin plugin)
========
public class StallThievingOverlay extends OverlayPanel {
    @Inject
    StallThievingOverlay(StallThievingPlugin plugin)
>>>>>>>> main:runelite-client/src/main/java/net/runelite/client/plugins/microbot/thieving/stalls/StallThievingOverlay.java
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
<<<<<<<< HEAD:runelite-client/src/main/java/net/runelite/client/plugins/microbot/DDAccountBuilder/DDAccountBuilderOverlay.java
                    .text("DDAccountBuilder V" + DDAccountBuilderScript.version)
========
                    .text("Stall Thieving V" + StallThievingScript.version)
>>>>>>>> main:runelite-client/src/main/java/net/runelite/client/plugins/microbot/thieving/stalls/StallThievingOverlay.java
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
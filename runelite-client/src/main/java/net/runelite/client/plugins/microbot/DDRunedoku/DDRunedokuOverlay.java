<<<<<<<< HEAD:runelite-client/src/main/java/net/runelite/client/plugins/microbot/DDRunedoku/DDRunedokuOverlay.java
package net.runelite.client.plugins.microbot.DDRunedoku;
========
package net.runelite.client.plugins.microbot.cooking;
>>>>>>>> main:runelite-client/src/main/java/net/runelite/client/plugins/microbot/cooking/AutoCookingOverlay.java

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

<<<<<<<< HEAD:runelite-client/src/main/java/net/runelite/client/plugins/microbot/DDRunedoku/DDRunedokuOverlay.java
public class DDRunedokuOverlay extends OverlayPanel {
    @Inject
    DDRunedokuOverlay(DDRunedokuPlugin plugin)
    {
========
public class AutoCookingOverlay extends OverlayPanel {
    @Inject
    AutoCookingOverlay(AutoCookingPlugin plugin) {
>>>>>>>> main:runelite-client/src/main/java/net/runelite/client/plugins/microbot/cooking/AutoCookingOverlay.java
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
<<<<<<<< HEAD:runelite-client/src/main/java/net/runelite/client/plugins/microbot/DDRunedoku/DDRunedokuOverlay.java
                    .text("DDRunedokuScript" + DDRunedokuScript.version)
========
                    .text("Micro Cooking V" + AutoCookingPlugin.version)
>>>>>>>> main:runelite-client/src/main/java/net/runelite/client/plugins/microbot/cooking/AutoCookingOverlay.java
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());

<<<<<<<< HEAD:runelite-client/src/main/java/net/runelite/client/plugins/microbot/DDRunedoku/DDRunedokuOverlay.java

========
>>>>>>>> main:runelite-client/src/main/java/net/runelite/client/plugins/microbot/cooking/AutoCookingOverlay.java
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}

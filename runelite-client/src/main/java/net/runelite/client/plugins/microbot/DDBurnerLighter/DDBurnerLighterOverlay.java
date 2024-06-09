package net.runelite.client.plugins.microbot.DDBurnerLighter;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static net.runelite.client.plugins.natepainthelper.Info.timeBegan;
import static net.runelite.client.plugins.natepainthelper.Info.xpGained;


public class DDBurnerLighterOverlay extends OverlayPanel {
    @Inject
    DDBurnerLighterOverlay(DDBurnerLighterPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    public long timeStart = System.currentTimeMillis();
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            long timeRanMs = System.currentTimeMillis() -timeStart;
            long timeRanHr = timeRanMs/3600000;
            long timeRanMin = (timeRanMs % 3600000)/60000;
            long timeRanSec = (timeRanMs % 60000) / 1000;

            panelComponent.setPreferredSize(new Dimension(300, 500));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("DDBurnerLighterScript " + DDBurnerLighterScript.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time Ran: " + timeRanHr + ":" + timeRanMin + ":" + timeRanSec )
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Current State: " + DDBurnerLighterScript.comment)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time Left for " + DDBurnerLighterScript.host1 +
                            (Microbot.getClient().getTickCount() - DDBurnerLighterScript.lightStartTimestamp[0]
                                    - DDBurnerLighterScript.incenseBurnDurationTick) + "ticks")
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time Left for " + DDBurnerLighterScript.host2 +
                            (Microbot.getClient().getTickCount() - DDBurnerLighterScript.lightStartTimestamp[1]
                                    - DDBurnerLighterScript.incenseBurnDurationTick) + "ticks")
                    .build());

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}

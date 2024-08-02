package net.runelite.client.plugins.microbot.DDBlastFurnace;

import net.runelite.api.Client;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.events.GrandExchangeSearched;
import net.runelite.api.geometry.Shapes;
import net.runelite.client.plugins.grandexchange.GrandExchangeClient;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.globval.WidgetIndices;
import net.runelite.client.plugins.microbot.util.mouse.Mouse;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class DDBlastFurnaceOverlay extends OverlayPanel {
    @Inject
    DDBlastFurnaceOverlay(DDBlastFurnacePlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            long timeRanMs = System.currentTimeMillis() -DDBlastFurnaceScript.timeStart;
            long timeRanHr = timeRanMs/3600000;
            long timeRanMin = (timeRanMs % 3600000)/60000;
            long timeRanSec = (timeRanMs % 60000) / 1000;
            panelComponent.setPreferredSize(new Dimension(200, 350));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("DDBlastFurnaceScript V:" + DDBlastFurnaceScript.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time Ran: " + timeRanHr + ":" + timeRanMin + ":" + timeRanSec)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Bars Smelted: "+DDBlastFurnaceScript.barsSmelted)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Dart Tips Made: "+DDBlastFurnaceScript.dartsMade)
                    .build());

            int profit = ((DDBlastFurnaceScript.barsSmelted*
                    (DDBlastFurnaceScript.barPrice - DDBlastFurnaceScript.primOrePrice - DDBlastFurnaceScript.secOrePrice))+
                    ((DDBlastFurnaceScript.dartsMade * DDBlastFurnaceScript.ironDartTipPrice) - (DDBlastFurnaceScript.ironBarPrice/10))-
                    DDBlastFurnaceScript.cofferAndForemanAndSipsSpent);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Profit: "+ profit)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Profit Per Hour: "+ ((profit* 3600000L)/timeRanMs))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("State: "+ DDBlastFurnaceScript.currentState)
                    .build());

            if(DDBlastFurnaceScript.currentState == DDBlastFurnaceScript.blastFurnanceStates.state_afk){
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Afk Time Till Log in: "+ DDBlastFurnaceScript.milliSecondTillLogin)
                        .build());
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}

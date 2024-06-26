package net.runelite.client.plugins.microbot.DDMoneyMoves;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface DDMoneyMovesConfig extends Config {
    /////////Bars Section/////////////
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String barsSection = "General";


}

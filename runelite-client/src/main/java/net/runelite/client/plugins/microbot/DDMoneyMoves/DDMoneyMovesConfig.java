package net.runelite.client.plugins.microbot.DDMoneyMoves;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface DDMoneyMovesConfig extends Config {
    /////////Bars Section/////////////
    @ConfigSection(
            name = "Blast Furnace",
            description = "Blast Furnace",
            position = 1,
            closedByDefault = false
    )
    String blastFurnaceReloadSection = "Blast Furnace";

    @ConfigItem(
            keyName = "Reload Resource Amt",
            name = "Reload Resource Amt",
            description = "Reload Resource Amt",
            position = 1,
            section = blastFurnaceReloadSection
    )
    default int reloadResourceAmount() {return 1000;}


}

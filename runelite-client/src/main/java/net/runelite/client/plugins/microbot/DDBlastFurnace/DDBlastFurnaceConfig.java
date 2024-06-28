package net.runelite.client.plugins.microbot.DDBlastFurnace;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import  net.runelite.client.plugins.microbot.DDBlastFurnace.enums.BlastFurnanceBars;

@ConfigGroup("example")
public interface DDBlastFurnaceConfig extends Config {
    /////////Bars Section/////////////
    @ConfigSection(
            name = "Bars",
            description = "Bars",
            position = 0,
            closedByDefault = false
    )
    String barsSection = "Bars to make";

    @ConfigItem(
            keyName = "Bars to Make",
            name = "Select Bar to make",
            description = "Bars to Make",
            position = 1,
            section = barsSection
    )
    default BlastFurnanceBars BlastFurnaceBarSelection() {return BlastFurnanceBars.NONE;}

    /////////Coffer Section////////////
    @ConfigSection(
            name = "Coffer",
            description = "Coffer",
            position = 1,
            closedByDefault = false
    )
    String cofferSection = "Coffer";

    @ConfigItem(
            keyName = "Coffer Reload Threshold",
            name = "Coffer Reload Threshold",
            description = "CofferThreshold",
            position = 0,
            section = cofferSection
    )
    default int cofferMinimum() {return 1000;}

    @ConfigItem(
            keyName = "Coffer Reload Amount",
            name = "Coffer Reload Amount",
            description = "Coffer Reload Amount",
            position = 1,
            section = cofferSection
    )
    default int cofferReloadAmount() {return 72000;}

    /////////Ice glove Section////////////
    @ConfigSection(
            name = "Ice Glove",
            description = "Ice Glove",
            position = 2,
            closedByDefault = false
    )
    String iceGloveSection = "Ice Glove";
    @ConfigItem(
            keyName = "Use Ice Glove",
            name = "Use Ice Glove",
            description = "Use Ice Glove",
            position = 1,
            section = iceGloveSection
    )
    default boolean useIceGlove() {return false;}

    ///////////////Stamina pot Section////////
    @ConfigSection(
            name = "Stamina Pot",
            description = "Stamina Pot",
            position = 3,
            closedByDefault = false
    )
    String StaminaPotSection = "Stamina Pot";

    @ConfigItem(
            keyName = "Use Stamina Pot",
            name = "Use Stamina Pot",
            description = "Use Stamina Pot",
            position = 1,
            section = StaminaPotSection
    )
    default boolean useStaminaPot() {return false;}

    /////////Restock Section/////////////
    @ConfigSection(
            name = "Restock",
            description = "Restock",
            position = 4,
            closedByDefault = false
    )
    String RestockSection = "How much to restock";

    @ConfigItem(
            keyName = "Restock Amount",
            name = "Restock Amount",
            description = "Restock Amount",
            position = 1,
            section = RestockSection
    )
    default int blastFurnaceRestockAmount() {return 50;}

}


package net.runelite.client.plugins.microbot.DDAdvertiser;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface DDAdvertiserConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "Message1",
            name = "Message1",
            description = "Message to type",
            position = 1,
            section = generalSection
    )
    default String message1() {return "House Open xgrace";}

    @ConfigItem(
            keyName = "Message2",
            name = "Message2",
            description = "Message to type",
            position = 2,
            section = generalSection
    )
    default String message2() {return "House Open xgrace";}

    @ConfigItem(
            keyName = "Message3",
            name = "Message3",
            description = "Message to type",
            position = 3,
            section = generalSection
    )
    default String message3() {return "House Open xgrace";}

    @ConfigItem(
            keyName = "pauseMinTime",
            name = "Pause Min Time",
            description = "Minimum amount of time to randomly pause for between message.",
            position = 4,
            section = generalSection
    )
    default int pauseMinTime()
    {
        return 10000;
    }

    @ConfigItem(
            keyName = "pauseMaxTime",
            name = "Pause Max Time",
            description = "Maximum amount of time to randomly pause for between message.",
            position = 4,
            section = generalSection
    )
    default int pauseMaxTime()
    {
        return 30000;
    }
}

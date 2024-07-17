package net.runelite.client.plugins.microbot.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface ExampleConfig extends Config {
    @ConfigSection(
            name = "Money Muling",
            description = "Money Muling",
            position = 5,
            closedByDefault = false
    )
    String MulingSection = "Money Laundry";

    @ConfigItem(
            keyName = "Muling Host",
            name = "Muling Host",
            description = "Muling Host",
            position = 1,
            section = MulingSection
    )
    default String mulingHost() {return "xGrace";}
}

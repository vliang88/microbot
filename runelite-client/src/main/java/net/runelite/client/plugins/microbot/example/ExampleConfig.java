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
            position = 1,
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

    @ConfigSection(
            name = "World selection",
            description = "World selection - must be PVP",
            position = 2,
            closedByDefault = false
    )
    String WorldSection = "World selection";

    @ConfigItem(
            keyName = "PVP World Number",
            name = "PVP World Number",
            description = "PVP World Number",
            position = 1,
            section = WorldSection
    )
    default int PVPWorldSelection() {return 579;}

    @ConfigItem(
            keyName = "Normal World Number",
            name = "Normal World Number",
            description = "Normal World Number",
            position = 2,
            section = WorldSection
    )
    default int normalWorldSelection() {return 330;}
}

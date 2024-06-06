package net.runelite.client.plugins.microbot.DDBurnerLighter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface DDBurnerLighterConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "HostHouse 1",
            name = "HostHouse 1",
            description = "Name of Host 1",
            position = 1,
            section = generalSection
    )
    default String message1() {return "xgrace";}

    @ConfigItem(
            keyName = "HostHouse 2",
            name = "HostHouse 2",
            description = "Name of Host 2",
            position = 2,
            section = generalSection
    )
    default String message2() {return "bedreiging";}

    @ConfigItem(
            keyName = "Use second house",
            name = "Use second house",
            description = "Use second house",
            position = 1,
            section = generalSection
    )
    default boolean useSecondHost() {
        return false;
    }
}

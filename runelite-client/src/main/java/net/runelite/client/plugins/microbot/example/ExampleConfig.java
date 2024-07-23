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


    @ConfigSection(
            name = "Eat Food Health percent",
            description = "Eat Food Health percent",
            position = 2,
            closedByDefault = false
    )
    String hitpointsSection = "Hitpoint food";

    @ConfigItem(
            keyName = "HP Percentage",
            name = "HP Percentage",
            description = "HP Percentage",
            position = 1,
            section = hitpointsSection
    )
    default int hitpoints() {return 50;}

    @ConfigSection(
            name = "Training 20-52",
            description = "Training 20-52. Go to Brimhaven spike trap",
            position = 3,
            closedByDefault = false
    )
    String trainingSection = "Training 20-52";

    @ConfigItem(
            keyName = "Do training",
            name = "Do training",
            description = "Do training",
            position = 1,
            section = trainingSection
    )
    default boolean doTraining() {return false;}
}

package net.runelite.client.plugins.microbot.DDBlastFurnace.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;

@Getter
@RequiredArgsConstructor
public enum BlastFurnanceBars {
    NONE("None", "None", "None",0, 0, 0, 0, 0, 0),
    Bronze("Bronze Bar", "Copper", "Tin",1, 0, Varbits.BLAST_FURNACE_BRONZE_BAR, ItemID.BRONZE_BAR, ItemID.COPPER_ORE, ItemID.TIN_ORE),
    Iron("Iron Bar", "Iron", "None",15, 0, Varbits.BLAST_FURNACE_IRON_BAR, ItemID.IRON_BAR, ItemID.IRON_ORE, 0),
    Steel("Steel Bar","Iron ore", "Coal", 30, 1, Varbits.BLAST_FURNACE_STEEL_BAR, ItemID.STEEL_BAR, ItemID.IRON_ORE, ItemID.COAL),
    Gold("Gold Bar", "Gold", "None",30, 0, Varbits.BLAST_FURNACE_GOLD_BAR, ItemID.GOLD_BAR, ItemID.GOLD_ORE, 0),
    Mithril("Mithril Bar", "Mithril ore", "Coal",50, 2, Varbits.BLAST_FURNACE_MITHRIL_BAR,ItemID.MITHRIL_BAR, ItemID.MITHRIL_ORE, ItemID.COAL),
    Adamantite("Adamantite Bar", "Adamantite ore", "Coal",70, 3, Varbits.BLAST_FURNACE_ADAMANTITE_BAR, ItemID.ADAMANTITE_BAR, ItemID.ADAMANTITE_ORE, ItemID.COAL),
    Rune("Rune Bar", "Runite ore", "Coal",85, 4, Varbits.BLAST_FURNACE_RUNITE_BAR, ItemID.RUNITE_BAR, ItemID.RUNITE_ORE, ItemID.COAL);

    private final String name;
    private final String primaryOre;
    private final String secondaryOre;
    private final int requiredLevel;
    private final int coalRequired;
    private final int varbit;
    private final int barId;
    private final int primaryId;
    private final int secondaryId;
}
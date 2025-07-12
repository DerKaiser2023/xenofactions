package com.hfr.achievements;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import com.hfr.blocks.ModBlocks;

public class AchievementEvents {

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        if (event.crafting.getItem() == ModBlocks.machineBlastFurnace.getItem()) {
            event.player.addStat(ModAchievements.craftBlastFurnace, 1);
        }
    }
}
package com.hfr.achievements;

import net.minecraft.stats.Achivement;
import net.minecraftforge.common.AchivementPage;
import com.hfr.items.ModItems;
import com.hfr.blocks.ModBlocks;

public class ModAchievements {
    public static Achivement placeBlastFurnace;

    public static AchivementPage page;

    public static  void init() {
        craftBlastFurnace = new Achivement("achivement.craftBlastFurnace", "craftBlastFurnace", 0, 0, ModBlocks.machineBlastFurnace, null).registerStat();

        page = new AchivementPage("HFR Achievements", craftBlastFurnace);
        AchivementPage.registerAchivementPage(page);
    }
}
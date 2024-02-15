package net.pitan76.spacecube.client.compat;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.pitan76.easyapi.FileControl;
import net.pitan76.mcpitanlib.api.util.TextUtil;
import net.minecraft.client.gui.screen.Screen;

import static net.pitan76.spacecube.Config.*;

public class ClothConfig {
    public static Screen create(Screen screen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(screen)
                .setTitle(TextUtil.translatable("title.spacecube.config"))
                .setSavingRunnable(() -> {
                    if (!FileControl.fileExists(getConfigDir())) {
                        getConfigDir().mkdirs();
                    }
                    config.save(getConfigFile());
                });
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(TextUtil.translatable("category.spacecube.general"));


        general.addEntry(entryBuilder.startDoubleField(TextUtil.translatable("option.spacecube.energy.rebornEnergyConversionRate"), config.getDouble("energy.rebornEnergyConversionRate"))
                .setDefaultValue(1.0)
                .setSaveConsumer(newValue -> config.setDouble("energy.rebornEnergyConversionRate", newValue))
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(TextUtil.translatable("option.spacecube.chunkloader"), config.getBoolean("chunkloader.enabled"))
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.setBoolean("chunkloader.enabled", newValue))
                .build());
        general.addEntry(entryBuilder.startIntField(TextUtil.translatable("option.spacecube.chunkloader.radius"), config.getInt("chunkloader.radius"))
                .setDefaultValue(2)
                .setSaveConsumer(newValue -> config.setInt("chunkloader.radius", newValue))
                .build());

        return builder.build();
    }
}

package net.pitan76.spacecube;

import net.pitan76.easyapi.FileControl;
import net.pitan76.easyapi.config.JsonConfig;

import java.io.File;

public class Config {
    public static File configDir;
    public static String fileName = SpaceCube.MOD_ID + ".json";

    public static JsonConfig config = new JsonConfig();

    public static boolean initialized = false;

    public static void init(File configDir) {
        //System.out.println("[Space Cube]" + configDir.toString());
        if (initialized) return;
        initialized = true;
        setConfigDir(configDir);

        defaultConfig();

        if (FileControl.fileExists(getConfigFile()))
            config.load(getConfigFile());
        else
            config.save(getConfigFile());

        // 既存の設定以外をデフォルト設定にする (Set to default settings except for existing settings)
        fixConfig();
    }

    public static File getConfigFile() {
        return new File(getConfigDir(), fileName);
    }

    public static void setConfigDir(File configDir) {
        Config.configDir = configDir;
    }

    public static File getConfigDir() {
        return configDir;
    }

    public static boolean reload() {
        if (FileControl.fileExists(getConfigFile())) {
            config.load(getConfigFile());
            return true;
        }
        return false;
    }

    public static void defaultConfig() {
        config.setDouble("energy.rebornEnergyConversionRate", 1.0);
        config.setBoolean("chunkloader.enabled", true);
        config.setInt("chunkloader.radius", 2);
    }

    public static void fixConfig() {
        if (config.has("chunkloader")) {
            config.configMap.remove("chunkloader");
        }

        if (!config.has("energy.rebornEnergyConversionRate"))
            config.setDouble("energy.rebornEnergyConversionRate", 1.0);

        if (!config.has("chunkloader.enabled"))
            config.setBoolean("chunkloader.enabled", true);

        if (!config.has("chunkloader.radius"))
            config.setInt("chunkloader.radius", 2);
    }

    public static void save() {
        config.save(getConfigFile());
    }

    public static double getRebornEnergyConversionRate() {
        try {
            return config.getDouble("energy.rebornEnergyConversionRate");
        } catch (NullPointerException e) {
            config.setDouble("energy.rebornEnergyConversionRate", 1.0);
            config.save(getConfigFile());
        }
        return 1.0;
    }

    public static boolean enabledChunkLoader() {
        try {
            return config.getBoolean("chunkloader.enabled");
        } catch (NullPointerException e) {
            config.setBoolean("chunkloader.enabled", true);
            config.save(getConfigFile());
        }
        return true;
    }

    public static int getChunkLoaderRadius() {
        try {
            return config.getInt("chunkloader.radius");
        } catch (NullPointerException e) {
            config.setInt("chunkloader.radius", 2);
            config.save(getConfigFile());
        }
        return 2;
    }
}

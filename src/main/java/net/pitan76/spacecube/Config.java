package net.pitan76.spacecube;

import ml.pkom.easyapi.FileControl;
import ml.pkom.easyapi.config.JsonConfig;

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

        config.setDouble("energy.rebornEnergyConversionRate", 1.0);
        config.setBoolean("chunkloader", true);
        config.setInt("chunkloader.radius", 2);

        if (FileControl.fileExists(getConfigFile()))
            config.load(getConfigFile());
        else
            config.save(getConfigFile());
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
            return config.getBoolean("chunkloader");
        } catch (NullPointerException e) {
            config.setBoolean("chunkloader", true);
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

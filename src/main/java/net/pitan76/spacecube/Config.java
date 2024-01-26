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
}

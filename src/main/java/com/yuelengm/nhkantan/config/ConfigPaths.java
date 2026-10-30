package com.yuelengm.nhkantan.config;

import java.nio.file.Path;

import cpw.mods.fml.common.Loader;

/**
 * Location of all NH kantan configuration files: {@code config/nh-kantan/}.
 */
public class ConfigPaths {

    /** Sub directory of the Minecraft config directory; a compile-time constant for use in annotations. */
    public static final String DIRECTORY_NAME = "nh-kantan";

    /** File name, without extension, of the GTNHLib managed {@code .cfg}. */
    public static final String CONFIG_FILE_NAME = "nhkantan";

    private ConfigPaths() {}

    public static Path directory() {
        return Loader.instance()
            .getConfigDir()
            .toPath()
            .resolve(DIRECTORY_NAME);
    }
}

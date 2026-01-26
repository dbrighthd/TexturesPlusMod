package com.dbrighthd.texturesplusmod.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "texturesplusmod")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean async = false;

    public boolean updatePacksOnStartup = true;

    @ConfigEntry.Gui.Tooltip
    public boolean elytraArmorStands = false;

    @ConfigEntry.Gui.Tooltip
    public boolean mergeEntities = true;

    @ConfigEntry.Gui.Tooltip
    public boolean sortAlphabetically = true;

    public boolean ignoreTexturesPlusMcmeta = true;

    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean devMode = false;

    @ConfigEntry.Gui.Tooltip(count = 3)
    public String githubApiKey = "";
}

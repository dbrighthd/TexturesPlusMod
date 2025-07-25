package com.dbrighthd.texturesplusmod.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "texturesplusmod")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip(count = 4)
    public boolean async = false;

    public boolean updatePacksOnStartup = true;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean makeWorld = false;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean elytraArmorStands = false;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean mergeEntities = true;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean sortAlphabetically = true;

    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean devMode = false;
}

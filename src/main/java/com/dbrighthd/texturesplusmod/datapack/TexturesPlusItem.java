package com.dbrighthd.texturesplusmod.datapack;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record TexturesPlusItem(List<String> enchantments, String rename, int damage, @NotNull String itemType, String model) {
    public TexturesPlusItem(List<String> enchantments, String rename, int damage, @NotNull String itemType, String model) {
        this.enchantments = List.copyOf(Objects.requireNonNullElse(enchantments, List.of()));
        this.rename = rename;
        this.damage = damage;
        this.itemType = itemType;
        this.model = model;
    }

}

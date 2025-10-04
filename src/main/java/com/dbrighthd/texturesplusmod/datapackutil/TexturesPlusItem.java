package com.dbrighthd.texturesplusmod.datapackutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TexturesPlusItem {
    public final String itemType;
    public final List<String> enchantments;
    public final String rename;
    public final int damage;
    public final String model;

    public TexturesPlusItem(List<String> enchantments, String rename, int damage, String itemType, String model) {
        this.enchantments = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNullElse(enchantments, List.of()))
        );
        this.rename = rename;
        this.damage = damage;
        this.itemType = itemType;
        this.model = model;
    }

}

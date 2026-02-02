package com.dbrighthd.texturesplusmod.datapack;

import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.SequencedCollection;

public interface ItemCategorySelector {
    SequencedCollection<ItemCategory> categories();
    Optional<ItemCategory> select(Identifier model);
}

package com.dbrighthd.texturesplusmod.datapack;

import java.util.List;

@FunctionalInterface
public interface ItemRowCreator {
    void createRow(StringBuilder sb, ItemCategory category, List<String> models);
}

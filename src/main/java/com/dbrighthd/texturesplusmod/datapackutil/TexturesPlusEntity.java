package com.dbrighthd.texturesplusmod.datapackutil;

import java.util.List;

public class TexturesPlusEntity {
    public final List<TexturesPlusEntityNbt> nbtList;
    public final String entityType;

    public TexturesPlusEntity(List<TexturesPlusEntityNbt> nbtList, String entityType) {
        this.nbtList = nbtList;
        this.entityType = entityType;
    }
}

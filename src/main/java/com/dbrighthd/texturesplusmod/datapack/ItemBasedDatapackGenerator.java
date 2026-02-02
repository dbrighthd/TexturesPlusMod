package com.dbrighthd.texturesplusmod.datapack;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.LOGGER;

public class ItemBasedDatapackGenerator {

    private final Path itemJsonPath;
    private final ItemRowCreator rowCreator;
    private final ItemCategorySelector categorySelector;

    public ItemBasedDatapackGenerator(Path itemJsonPath, ItemCategorySelector itemCategorySelector, ItemRowCreator rowCreator) {
        this.itemJsonPath = itemJsonPath;
        this.rowCreator = rowCreator;
        this.categorySelector = itemCategorySelector;
    }

    public DataResult<String> generateCommands() {
        JsonObject root;
        try (FileReader reader = new FileReader(itemJsonPath.toFile())) {
            root = GsonHelper.parse(reader);
        } catch (IOException e) {
            return DataResult.error(e::getLocalizedMessage);
        }

        Map<ItemCategory, List<String>> categories = new HashMap<>();
        categorySelector.categories().forEach(category -> categories.putIfAbsent(category, new ArrayList<>()));

        DataResult<ClientItem> item = ClientItem.CODEC.parse(JsonOps.INSTANCE, root);
        if (item.isError()) return DataResult.error(item.error().orElseThrow().messageSupplier());
        if (item.getOrThrow().model() instanceof SelectItemModel.Unbaked select) { // "SelectItemModel" is the "select" model type in the json. See the ItemModels vanilla class to see all the valid model types and what they map to in the json file.
            if (!(select.unbakedSwitch().property() instanceof ComponentContents<?>(DataComponentType<?> componentType))) return DataResult.error(() -> "Item model isn't filtering by data components."); // we only support models based on the name of the item, and this is the switch case that does the item name
            if (!componentType.equals(DataComponents.CUSTOM_NAME)) return DataResult.error(() -> "Item model isn't filtering by custom name."); // by checking this we can guarantee that
            select.unbakedSwitch().cases().forEach(c -> {
                List<?> l = c.values();
                if (!(l.getFirst() instanceof Component component)) return; // CUSTOM_NAME is of type Component so that means c.values() is of type component.

                ItemModel.Unbaked model = c.model();
                Identifier modelId;
                switch (model) {
                    case BlockModelWrapper.Unbaked bmodel ->  // a "BlockModelWrapper" model is a "basic" item model, the "model" type model
                            modelId = bmodel.model();
                    case SelectItemModel.Unbaked smodel -> {
                        ItemModel.Unbaked fallback = smodel.fallback().orElseThrow();
                        if (!(fallback instanceof BlockModelWrapper.Unbaked bmodel)) { // we could keep going and keep going down this path, but it would suck so
                            LOGGER.error("{} had a weird model in it, skipping... (fallback is a complex model)", itemJsonPath.toAbsolutePath());
                            return;
                        }
                        modelId = bmodel.model();
                    }
                    case ConditionalItemModel.Unbaked cmodel -> {
                        ItemModel.Unbaked onFalse = cmodel.onFalse();
                        if (!(onFalse instanceof BlockModelWrapper.Unbaked bmodel)) { // we could keep going and keep going down this path, but it would suck so
                            LOGGER.error("{} had a weird model in it, skipping... (condition on_false is a complex model)", itemJsonPath.toAbsolutePath());
                            return;
                        }
                        modelId = bmodel.model();
                    }
                    case null, default -> {
                        LOGGER.error("{} had a weird model in it, skipping... (model case is too complex for this parser)", itemJsonPath.toAbsolutePath());
                        return;
                    }
                }

                Optional<ItemCategory> optionalCategory = categorySelector.select(modelId);
                optionalCategory.ifPresent(category -> categories.get(category).add(component.tryCollapseToString()));
            });
        } else {
            return DataResult.error(() -> "bad model in json");
        }

        StringBuilder sb = new StringBuilder();
        for (var entry : categories.entrySet()) {
            rowCreator.createRow(sb, entry.getKey(), entry.getValue().stream().sorted().toList());
        }

        return DataResult.success(sb.toString());
    }
}

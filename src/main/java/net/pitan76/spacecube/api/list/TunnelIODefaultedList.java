package net.pitan76.spacecube.api.list;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.ItemTunnel;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity.indexToDir;

public class TunnelIODefaultedList extends DefaultedList<ItemStack> {

    private final SpaceCubeBlockEntity blockEntity;

    protected TunnelIODefaultedList(List<ItemStack> delegate, @Nullable ItemStack initialElement, SpaceCubeBlockEntity blockEntity) {
        super(delegate, initialElement);
        this.blockEntity = blockEntity;
    }


    public static TunnelIODefaultedList ofSize(SpaceCubeBlockEntity entity) {
        return ofSize(ItemTunnel.defaultSize * Direction.values().length, ItemStack.EMPTY, entity);
    }


    public static TunnelIODefaultedList ofSize(int size, ItemStack defaultValue, SpaceCubeBlockEntity entity) {
        Validate.notNull(defaultValue);
        ItemStack[] objects = new ItemStack[size];
        Arrays.fill(objects, defaultValue);
        return new TunnelIODefaultedList(Arrays.asList(objects), defaultValue, entity);
    }

    @Override
    public ItemStack set(int index, ItemStack stack) {
        if (!blockEntity.hasTunnelType(TunnelType.ITEM)) return ItemStack.EMPTY;
        Direction dir = indexToDir(Math.floorDiv(index, 2));

        ItemTunnel itemTunnel = (ItemTunnel) blockEntity.getTunnelDef(TunnelType.ITEM, dir);
        if (itemTunnel == null) return ItemStack.EMPTY;

        if (index % 2 == 0) {
            // 偶数なのでImportStack
            itemTunnel.setImportStack(stack);
        } else {
            // 奇数なのでExportStack
            itemTunnel.setExportStack(stack);
        }
        return super.set(index, stack);
    }

    @Override
    public ItemStack get(int index) {
        if (!blockEntity.hasTunnelType(TunnelType.ITEM)) return ItemStack.EMPTY;
        Direction dir = indexToDir(Math.floorDiv(index, 2));

        ItemTunnel itemTunnel = (ItemTunnel) blockEntity.getTunnelDef(TunnelType.ITEM, dir);
        if (itemTunnel == null) return ItemStack.EMPTY;

        if (index % 2 == 0) {
            // 偶数なのでImportStack
            return itemTunnel.getImportStack();
        } else {
            // 奇数なのでExportStack
            return itemTunnel.getExportStack();
        }
    }
}

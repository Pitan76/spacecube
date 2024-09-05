package net.pitan76.spacecube.api.tunnel.def;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.pitan76.mcpitanlib.api.util.NbtUtil;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public class ItemTunnel implements ITunnelDef {
    private TunnelWallBlockEntity blockEntity = null;

    public ItemTunnel(TunnelWallBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public TunnelType getTunnelType() {
        return TunnelType.ITEM;
    }

    @Override
    public TunnelWallBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public static int defaultSize = 2;

    // 一時的な保管場所であり、保存されないことに注意 (Temporary storage and not saved)
    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(defaultSize, ItemStack.EMPTY);

    public ItemStack getExportStack() {
        return stacks.get(1);
    }

    public ItemStack getImportStack() {
        return stacks.get(0);
    }

    public void setExportStack(ItemStack stack) {
        stacks.set(1, stack);
    }

    public void setImportStack(ItemStack stack) {
        stacks.set(0, stack);
    }

    public int getStackSize() {
        return getStacks().size();
    }

    public DefaultedList<ItemStack> getStacks() {
        return stacks;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt == null) return;
        if (nbt.contains("importStack"))
            setImportStack(ItemStack.fromNbt(nbt.getCompound("importStack")));
        if (nbt.contains("exportStack"))
            setExportStack(ItemStack.fromNbt(nbt.getCompound("exportStack")));
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        if (nbt == null) nbt = NbtUtil.create();
        if (!getImportStack().isEmpty())
            nbt.put("importStack", getImportStack().writeNbt(new NbtCompound()));
        if (!getExportStack().isEmpty())
            nbt.put("exportStack", getExportStack().writeNbt(new NbtCompound()));
    }
}

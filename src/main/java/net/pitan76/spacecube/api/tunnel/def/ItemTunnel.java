package net.pitan76.spacecube.api.tunnel.def;

import net.minecraft.item.ItemStack;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.util.ItemStackUtil;
import net.pitan76.mcpitanlib.api.util.NbtUtil;
import net.pitan76.mcpitanlib.api.util.collection.ItemStackList;
import net.pitan76.mcpitanlib.api.util.nbt.v2.NbtRWUtil;
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
    private final ItemStackList stacks = ItemStackList.ofSize(defaultSize, ItemStackUtil.empty());

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

    public ItemStackList getStacks() {
        return stacks;
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        if (args == null) return;
        NbtRWUtil.getItemStack(args, "importStack").ifPresent(this::setImportStack);
        NbtRWUtil.getItemStack(args, "exportStack").ifPresent(this::setExportStack);
    }

    @Override
    public void writeNbt(WriteNbtArgs args) {
        if (args == null) args = new WriteNbtArgs(NbtUtil.create());
        if (!getImportStack().isEmpty()) {
            NbtRWUtil.putItemStack(args, "importStack", getImportStack());
        }

        if (!getExportStack().isEmpty()) {
            NbtRWUtil.putItemStack(args, "exportStack", getExportStack());
        }
    }
}

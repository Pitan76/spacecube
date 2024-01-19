package net.pitan76.spacecube.api.tunnel.def;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
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

    public static int defaultSize = 1;

    private final DefaultedList<ItemStack> importStacks = DefaultedList.ofSize(defaultSize, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> exportStacks = DefaultedList.ofSize(defaultSize, ItemStack.EMPTY);

    public DefaultedList<ItemStack> getExportStacks() {
        return exportStacks;
    }

    public DefaultedList<ItemStack> getImportStacks() {
        return importStacks;
    }

    public int getImportStackSize() {
        return importStacks.size();
    }

    public int getExportStackSize() {
        return exportStacks.size();
    }

    public void setImportStacks(DefaultedList<ItemStack> stacks) {
        for (int i = 0; i < getImportStackSize(); i++) {
            importStacks.set(i, stacks.get(i));
        }
    }

    public void setExportStacks(DefaultedList<ItemStack> stacks) {
        for (int i = 0; i < getExportStackSize(); i++) {
            exportStacks.set(i, stacks.get(i));
        }
    }

    public void setImportStack(int index, ItemStack stack) {
        importStacks.set(index, stack);
    }

    public void setExportStack(int index, ItemStack stack) {
        exportStacks.set(index, stack);
    }

    public ItemStack getImportStack(int index) {
        return importStacks.get(index);
    }

    public ItemStack getExportStack(int index) {
        return exportStacks.get(index);
    }

    public void clearImportStacks() {
        for (int i = 0; i < getImportStackSize(); i++) {
            importStacks.set(i, ItemStack.EMPTY);
        }
    }

    public void clearExportStacks() {
        for (int i = 0; i < getExportStackSize(); i++) {
            exportStacks.set(i, ItemStack.EMPTY);
        }
    }

    public void clearImportStack(int index) {
        importStacks.set(index, ItemStack.EMPTY);
    }

    public void clearExportStack(int index) {
        exportStacks.set(index, ItemStack.EMPTY);
    }

    public int getStackSize() {
        return getImportStackSize() + getExportStackSize();
    }

    public DefaultedList<ItemStack> getStacks() {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(getStackSize(), ItemStack.EMPTY);
        for (int i = 0; i < getImportStackSize(); i++) {
            stacks.set(i, importStacks.get(i));
        }
        for (int i = 0; i < getExportStackSize(); i++) {
            stacks.set(i + getImportStackSize(), exportStacks.get(i));
        }
        return stacks;
    }

    public void setStacks(DefaultedList<ItemStack> stacks) {
        for (int i = 0; i < getImportStackSize(); i++) {
            importStacks.set(i, stacks.get(i));
        }
        for (int i = 0; i < getExportStackSize(); i++) {
            exportStacks.set(i, stacks.get(i + getImportStackSize()));
        }
    }

    public void clearStacks() {
        clearImportStacks();
        clearExportStacks();
    }

    public void clearStack(int index) {
        if (index < getImportStackSize()) {
            clearImportStack(index);
        } else {
            clearExportStack(index - getImportStackSize());
        }
    }

    public ItemStack getStack(int index) {
        if (index < getImportStackSize()) {
            return getImportStack(index);
        } else {
            return getExportStack(index - getImportStackSize());
        }
    }

    public void setStack(int index, ItemStack stack) {
        if (index < getImportStackSize()) {
            setImportStack(index, stack);
        } else {
            setExportStack(index - getImportStackSize(), stack);
        }
    }

    public void addExportStack(ItemStack stack) {
        exportStacks.add(stack);
    }

    public void addImportStack(ItemStack stack) {
        importStacks.add(stack);
    }

    public void removeExportStack(int index) {
        exportStacks.remove(index);
    }

    public void removeImportStack(int index) {
        importStacks.remove(index);
    }

    public void addStack(int index, ItemStack stack) {
        if (index < getImportStackSize()) {
            setImportStack(index, stack);
        } else {
            setExportStack(index - getImportStackSize(), stack);
        }
    }

    public boolean isEmpty() {
        for (int i = 0; i < getImportStackSize(); i++) {
            if (!getImportStack(i).isEmpty()) return false;
        }
        for (int i = 0; i < getExportStackSize(); i++) {
            if (!getExportStack(i).isEmpty()) return false;
        }
        return true;
    }
}

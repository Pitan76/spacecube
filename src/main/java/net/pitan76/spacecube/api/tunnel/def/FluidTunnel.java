package net.pitan76.spacecube.api.tunnel.def;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.nbt.NbtCompound;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

@SuppressWarnings("UnstableApiUsage")
public class FluidTunnel implements ITunnelDef {
    private TunnelWallBlockEntity blockEntity = null;

    public FluidTunnel(TunnelWallBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public TunnelType getTunnelType() {
        return TunnelType.FLUID;
    }

    @Override
    public TunnelWallBlockEntity getBlockEntity() {
        return blockEntity;
    }

    private final SingleVariantStorage<FluidVariant> fluidStorage = new SingleVariantStorage<FluidVariant>() {
        @Override
        public FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        public long getCapacity(FluidVariant variant) {
            return FluidConstants.BUCKET * 8;
        }

        @Override
        protected void onFinalCommit() {
            blockEntity.markDirty();
        }
    };

    public boolean isEmpty() {
        return fluidStorage.amount == 0;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.put("variant", fluidStorage.variant.toNbt());
        nbt.putLong("amount", fluidStorage.amount);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        fluidStorage.variant = FluidVariant.fromNbt(nbt.getCompound("variant"));
        fluidStorage.amount = nbt.getLong("amount");
    }

    public SingleVariantStorage<FluidVariant> getFluidStorage() {
        return fluidStorage;
    }
}

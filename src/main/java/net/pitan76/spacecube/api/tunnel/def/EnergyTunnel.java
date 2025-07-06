package net.pitan76.spacecube.api.tunnel.def;

import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.util.nbt.v2.NbtRWUtil;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import net.pitan76.spacecube.compat.TunnelEnergyStorage;

public class EnergyTunnel implements ITunnelDef {
    private TunnelWallBlockEntity blockEntity = null;

    public EnergyTunnel(TunnelWallBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public TunnelType getTunnelType() {
        return TunnelType.ENERGY;
    }

    @Override
    public TunnelWallBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public TunnelEnergyStorage energyStorage = null;

    public TunnelEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public void setEnergyStorage(TunnelEnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    public boolean hasEnergyStorage() {
        return energyStorage != null;
    }

    public long energy = 0;

    public long getEnergy() {
        return energy;
    }

    public void setEnergy(long energy) {
        this.energy = energy;
    }

    public long getMaxEnergy() {
        return 10_000;
    }

    public long getUsableCapacity() {
        return getMaxEnergy() - energy;
    }

    public long insertEnergy(long amount) {
        long usableCapacity = getUsableCapacity();
        if (amount > usableCapacity) {
            energy += usableCapacity;
            return usableCapacity;
        }
        energy += amount;
        return amount;
    }

    public long extractEnergy(long amount) {
        if (amount > energy) {
            long energy = this.energy;
            this.energy = 0;
            return energy;
        }
        energy -= amount;
        return amount;
    }

    public boolean isEmpty() {
        return energy == 0;
    }

    public boolean isFull() {
        return energy == getMaxEnergy();
    }

    @Override
    public void writeNbt(WriteNbtArgs args) {
        NbtRWUtil.putLong(args, "energy", energy);
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        energy = NbtRWUtil.getLong(args, "energy");
    }
}

package net.pitan76.spacecube.compat;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.pitan76.spacecube.Config;
import net.pitan76.spacecube.api.tunnel.def.EnergyTunnel;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings("UnstableApiUsage")
public class TunnelEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {

    public static final double ENERGY_RATE = Config.config.getDouble("energy.rebornEnergyConversionRate");

    private final EnergyTunnel tunnel;

    public TunnelEnergyStorage(EnergyTunnel tunnel) {
        this.tunnel = tunnel;
    }

    public EnergyTunnel getTunnel() {
        return tunnel;
    }

    public TunnelWallBlockEntity getBlockEntity() {
        return tunnel.getBlockEntity();
    }

    public long getUsableCapacity() {
        return (long) (tunnel.getUsableCapacity() / ENERGY_RATE);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        if (maxAmount < getUsableCapacity()) {
            updateSnapshots(transaction);
            return (long) (tunnel.insertEnergy((long) (maxAmount * ENERGY_RATE)) / ENERGY_RATE);
        }
        return 0;

    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        if (maxAmount < getAmount()) {
            updateSnapshots(transaction);
            return (long) (tunnel.extractEnergy((long) (maxAmount * ENERGY_RATE)) / ENERGY_RATE);
        }
        return 0;
    }

    @Override
    public long getAmount() {
        return (long) (tunnel.getEnergy() / ENERGY_RATE);
    }

    @Override
    public long getCapacity() {
        return (long) (tunnel.getMaxEnergy() / ENERGY_RATE);
    }

    @Override
    protected Long createSnapshot() {
        return tunnel.getEnergy();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        tunnel.setEnergy(snapshot);
    }
}

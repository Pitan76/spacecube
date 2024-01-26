package net.pitan76.spacecube.compat;

import net.pitan76.spacecube.BlockEntities;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.EnergyTunnel;
import net.pitan76.spacecube.api.tunnel.def.ITunnelDef;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import team.reborn.energy.api.EnergyStorage;

public class RebornEnergyRegister {
    public static void init() {
        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, dir) -> {
            if (blockEntity instanceof TunnelWallBlockEntity) {
                ITunnelDef def = ((TunnelWallBlockEntity) blockEntity).getTunnelDef();
                if (def instanceof EnergyTunnel) {
                    EnergyTunnel energyTunnel = (EnergyTunnel) def;
                    if (!energyTunnel.hasEnergyStorage()) energyTunnel.setEnergyStorage(new TREnergyStorage(energyTunnel));

                    if (energyTunnel.getEnergyStorage() instanceof TREnergyStorage)
                        return (TREnergyStorage) energyTunnel.getEnergyStorage();
                }
            }

            return null;
        }, BlockEntities.TUNNEL_WALL_BLOCK_ENTITY.getOrNull());

        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, dir) -> {
            if (blockEntity instanceof SpaceCubeBlockEntity) {
                SpaceCubeBlockEntity scBlockEntity = (SpaceCubeBlockEntity) blockEntity;
                ITunnelDef def = scBlockEntity.getTunnelDef(TunnelType.ENERGY, dir);
                if (def instanceof EnergyTunnel) {
                    EnergyTunnel energyTunnel = (EnergyTunnel) def;
                    if (!energyTunnel.hasEnergyStorage()) energyTunnel.setEnergyStorage(new TREnergyStorage(energyTunnel));

                    if (energyTunnel.getEnergyStorage() instanceof TREnergyStorage)
                        return (TREnergyStorage) energyTunnel.getEnergyStorage();
                }
            }

            return null;
        }, BlockEntities.SPACE_CUBE_BLOCK_ENTITY.getOrNull());

    }
}

package net.pitan76.spacecube.api.data;

import net.pitan76.spacecube.api.tunnel.TunnelType;

public class TunnelWallBlockEntityRenderAttachmentData {
    public TunnelType tunnelType = TunnelType.NONE;

    public TunnelWallBlockEntityRenderAttachmentData(TunnelType tunnelType) {
        this.tunnelType = tunnelType;
    }

    public TunnelWallBlockEntityRenderAttachmentData() {

    }

    public TunnelType getTunnelType() {
        return tunnelType;
    }

    public void setTunnelType(TunnelType tunnelType) {
        this.tunnelType = tunnelType;
    }
}

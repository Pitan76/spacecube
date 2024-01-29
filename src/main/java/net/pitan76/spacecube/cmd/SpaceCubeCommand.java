package net.pitan76.spacecube.cmd;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.pkom.mcpitanlibarch.api.command.CommandSettings;
import ml.pkom.mcpitanlibarch.api.command.LiteralCommand;
import ml.pkom.mcpitanlibarch.api.event.ServerCommandEvent;
import ml.pkom.mcpitanlibarch.api.util.TextUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.pitan76.spacecube.Config;
import net.pitan76.spacecube.item.PersonalShrinkingDevice;

import java.util.Optional;

public class SpaceCubeCommand extends LiteralCommand {
    @Override
    public void init(CommandSettings settings) {
        addArgumentCommand("return", new LiteralCommand() {
            @Override
            public void execute(ServerCommandEvent event) {
                try {
                    PersonalShrinkingDevice.tpPrevCubeOrWorld(event.getWorld(), event.getPlayer());
                } catch (CommandSyntaxException e) {
                    event.sendFailure(TextUtil.literal(e.getMessage()));
                }
            }
        });

        addArgumentCommand("reload", new LiteralCommand() {
            @Override
            public void init(CommandSettings settings) {
                settings.permissionLevel(2);
            }

            @Override
            public void execute(ServerCommandEvent event) {
                event.sendSuccess(TextUtil.literal("[SpaceCube] Reloading..."), false);
                if (Config.reload()) {
                    event.sendSuccess(TextUtil.literal("[SpaceCube] Reloaded!"), false);
                } else {
                    event.sendFailure(TextUtil.literal("[SpaceCube] Failed to reload!"));
                }
            }
        });

        addArgumentCommand("config", new ConfigCommand());

        addArgumentCommand("version", new LiteralCommand() {
            @Override
            public void execute(ServerCommandEvent event) {
                Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("spacecube");
                if (!modContainer.isPresent()) {
                    event.sendFailure(TextUtil.literal("[SpaceCube] Failed to get version!"));
                    return;
                }
                event.sendSuccess(TextUtil.literal("[SpaceCube] v" + modContainer.get().getMetadata().getVersion()), false);
            }
        });
    }

    @Override
    public void execute(ServerCommandEvent event) {
        event.sendSuccess(TextUtil.literal("[SpaceCube]"
                + "\n- /spacecube version...Show version"
                + "\n- /spacecube reload...Reload config"
                + "\n- /spacecube config set [Key] [Value]...Set config"
                + "\n- /spacecube config get [Key]...Get config"
                + "\n- /spacecube config list...List config"
                + "\n- /spacecube config reset...Reset config"
                + "\n- /spacecube return...Return to the previous world"
        ), false);
    }
}

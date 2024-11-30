package net.pitan76.spacecube.cmd;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.pitan76.mcpitanlib.api.command.CommandSettings;
import net.pitan76.mcpitanlib.api.command.LiteralCommand;
import net.pitan76.mcpitanlib.api.event.ServerCommandEvent;
import net.pitan76.mcpitanlib.api.util.TextUtil;
import net.pitan76.mcpitanlib.midohra.world.World;
import net.pitan76.spacecube.Config;
import net.pitan76.spacecube.item.PersonalShrinkingDevice;

import java.util.Optional;

public class SpaceCubeCommand extends LiteralCommand {
    @Override
    public void init(CommandSettings settings) {
        addArgumentCommand("return", new LiteralCommand() {
            @Override
            public void execute(ServerCommandEvent e) {
                try {
                    PersonalShrinkingDevice.tpPrevCubeOrWorld(World.of(e.getWorld()), e.getPlayer());
                } catch (CommandSyntaxException ex) {
                    e.sendFailure(TextUtil.literal(ex.getMessage()));
                }
            }
        });

        addArgumentCommand("reload", new LiteralCommand() {
            @Override
            public void init(CommandSettings settings) {
                settings.permissionLevel(2);
            }

            @Override
            public void execute(ServerCommandEvent e) {
                e.sendSuccess(TextUtil.literal("[SpaceCube] Reloading..."), false);
                if (Config.reload()) {
                    e.sendSuccess(TextUtil.literal("[SpaceCube] Reloaded!"), false);
                } else {
                    e.sendFailure(TextUtil.literal("[SpaceCube] Failed to reload!"));
                }
            }
        });

        addArgumentCommand("config", new ConfigCommand());

        addArgumentCommand("version", new LiteralCommand() {
            @Override
            public void execute(ServerCommandEvent e) {
                Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("spacecube");
                if (!modContainer.isPresent()) {
                    e.sendFailure(TextUtil.literal("[SpaceCube] Failed to get version!"));
                    return;
                }
                e.sendSuccess(TextUtil.literal("[SpaceCube] v" + modContainer.get().getMetadata().getVersion()), false);
            }
        });
    }

    @Override
    public void execute(ServerCommandEvent e) {
        e.sendSuccess(TextUtil.literal("[SpaceCube]"
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

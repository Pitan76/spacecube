package net.pitan76.spacecube.cmd;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.pkom.mcpitanlibarch.api.command.CommandSettings;
import ml.pkom.mcpitanlibarch.api.command.LiteralCommand;
import ml.pkom.mcpitanlibarch.api.event.ServerCommandEvent;
import ml.pkom.mcpitanlibarch.api.util.TextUtil;
import net.pitan76.spacecube.Config;
import net.pitan76.spacecube.item.PersonalShrinkingDevice;

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
                event.sendSuccess(TextUtil.literal("Reloading..."), false);
                if (Config.reload()) {
                    event.sendSuccess(TextUtil.literal("Reloaded!"), false);
                } else {
                    event.sendFailure(TextUtil.literal("Failed to reload!"));
                }
            }
        });

        addArgumentCommand("config", new ConfigCommand());
    }

    @Override
    public void execute(ServerCommandEvent event) {
        event.sendSuccess(TextUtil.literal("[SpaceCube]"
                + "\n- /spacecube reload...Reload config"
                + "\n- /spacecube config set [Key] [Value]...Set config"
                + "\n- /spacecube config get [Key]...Get config"
                + "\n- /spacecube config list...List config"
                + "\n- /spacecube config reset...Reset config"
                + "\n- /spacecube return...Return to the previous world"
        ), false);
    }
}

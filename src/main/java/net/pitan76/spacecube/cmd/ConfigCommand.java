package net.pitan76.spacecube.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.pitan76.mcpitanlib.api.command.CommandSettings;
import net.pitan76.mcpitanlib.api.command.LiteralCommand;
import net.pitan76.mcpitanlib.api.command.argument.StringCommand;
import net.pitan76.mcpitanlib.api.event.ServerCommandEvent;
import net.pitan76.mcpitanlib.api.event.StringCommandEvent;
import net.pitan76.mcpitanlib.api.util.TextUtil;
import net.pitan76.spacecube.Config;

public class ConfigCommand extends LiteralCommand {

    @Override
    public void init(CommandSettings settings) {
        settings.permissionLevel(3);

        addArgumentCommand("set", new StringCommand() {
            @Override
            public String getArgumentName() {
                return "key";
            }

            @Override
            public void init(CommandSettings settings) {

                addArgumentCommand("value", new StringCommand() {
                    @Override
                    public String getArgumentName() {
                        return "value";
                    }

                    @Override
                    public void execute(StringCommandEvent event) {
                        String key = StringArgumentType.getString(event.context, "key");
                        String value = StringArgumentType.getString(event.context, "value");
                        if (Config.config.get(key) == null) {
                            event.sendFailure(TextUtil.literal("[SpaceCube] Key not found."));
                            return;
                        }
                        if (Config.config.get(key).getClass() == String.class) {
                            Config.config.setString(key, value);
                            event.sendSuccess(TextUtil.literal("[SpaceCube] Set " + key + " to " + value), false);

                        } else if (Config.config.get(key).getClass() == Integer.class) {
                            Config.config.setInt(key, Integer.parseInt(value));
                            event.sendSuccess(TextUtil.literal("[SpaceCube] Set " + key + " to " + value), false);

                        } else if (Config.config.get(key).getClass() == Double.class) {
                            Config.config.setDouble(key, Double.parseDouble(value));
                            event.sendSuccess(TextUtil.literal("[SpaceCube] Set " + key + " to " + value), false);

                        } else if (Config.config.get(key).getClass() == Boolean.class) {
                            Config.config.setBoolean(key, Boolean.parseBoolean(value));
                            event.sendSuccess(TextUtil.literal("[SpaceCube] Set " + key + " to " + value), false);

                        } else {
                            event.sendFailure(TextUtil.literal("[SpaceCube] Not supported type."));
                        }
                        Config.save();
                    }
                });
            }

            @Override
            public void execute(StringCommandEvent event) {
            }
        });

        addArgumentCommand("get", new StringCommand() {
            @Override
            public String getArgumentName() {
                return "key";
            }

            @Override
            public void execute(StringCommandEvent event) {
                String key = StringArgumentType.getString(event.context, "key");
                if (Config.config.get(key) == null) {
                    event.sendFailure(TextUtil.literal("[SpaceCube] Key not found."));
                    return;
                }
                event.sendSuccess(TextUtil.literal("[SpaceCube] " + key + ": " + Config.config.get(key).toString()), false);
            }
        });

        addArgumentCommand("list", new LiteralCommand() {
            @Override
            public void execute(ServerCommandEvent event) {
                event.sendSuccess(TextUtil.literal("[SpaceCube] Config List"), false);
                for (String key : Config.config.configMap.keySet()) {
                    event.sendSuccess(TextUtil.literal(" - " + key + ": " + Config.config.get(key).toString()), false);
                }
            }
        });

        addArgumentCommand("reset", new LiteralCommand() {
            @Override
            public void execute(ServerCommandEvent event) {
                Config.defaultConfig();
                Config.save();
                event.sendSuccess(TextUtil.literal("[SpaceCube] Reset config."), false);
            }
        });
    }

    @Override
    public void execute(ServerCommandEvent event) {
    }
}

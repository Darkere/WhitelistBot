package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Config.Config;
import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.Server.ServerList;
import com.darkere.whitelistbot.UserDataHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ReloadConfigCommand  implements ICommand {
    @Override
    public SlashCommandData registerCommand() {
        return makeCommand();
    }

    @Override
    public String getDescription() {
        return "Reloads the Config File";
    }

    @Override
    public String getName() {
        return "ReloadConfig";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Config.get().loadConfig();
        UserDataHandler.loadUserData();
        ServerList.get().forEachServer(Server::loadWhitelist);
        event.getHook().sendMessage("Reloaded").queue();
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}

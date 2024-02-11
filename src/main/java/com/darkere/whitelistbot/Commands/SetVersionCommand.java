package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.Util;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class SetVersionCommand implements ICommand{
    @Override
    public SlashCommandData registerCommand() {
        return makeCommand()
            .addOptions(CommandFunctions.getServerChoice(null))
            .addOption(OptionType.STRING,"version","version to set the server to", true);
    }

    @Override
    public String getDescription() {
        return "set the version of a server";
    }

    @Override
    public String getName() {
        return "setversion";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Server server = CommandFunctions.getServer(event);
        server.setVersion(event.getOption("version").getAsString());
        Util.updateStatusMessage(event.getJDA());
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}

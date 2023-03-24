package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Server.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PingCommand implements ICommand{
    @Override
    public SlashCommandData registerCommand() {
        return makeCommand()
            .addOptions(CommandFunctions.getShareOption());
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Server server = CommandFunctions.getServer(event);
        String command = event.getOption("command").getAsString();
        String answer = server.sendToServer(command);
        event.getHook().sendMessage(answer.isEmpty() ? "Offline" : answer).setEphemeral(CommandFunctions.getShareResult(event)).queue();
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}

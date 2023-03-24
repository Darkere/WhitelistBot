package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Server.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class RunCommand implements ICommand{
    @Override
    public SlashCommandData registerCommand() {
        return makeCommand()
            .addOptions(CommandFunctions.getServerChoice(null))
            .addOption(OptionType.STRING,"command","Command to run")
            .addOptions(CommandFunctions.getShareOption());
    }

    @Override
    public String getDescription() {
        return "run command on server";
    }

    @Override
    public String getName() {
        return "run";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Server server = CommandFunctions.getServer(event);
        String answer = server.sendToServer(event.getOption("command").getAsString());
        event.getHook().sendMessage(answer.isEmpty() ? "Offline" : answer).setEphemeral(CommandFunctions.getShareResult(event)).queue();
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}

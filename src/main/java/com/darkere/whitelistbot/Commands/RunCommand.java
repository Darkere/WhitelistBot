package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.Util;
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
        var option = event.getOption("command");
        if(option == null) {
            Util.send(event.reply("Unable to parse Command"));
            return;
        }
        String command = option.getAsString();
        String answer = server.sendToServer(command);
        Util.sendWithLog(event.getHook().sendMessage(answer.isEmpty() ? "Offline" : answer),event.getUser(),"ran Command " +  command + " on server " + server + " answer was " + answer);
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}

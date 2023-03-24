package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Server.ServerList;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ActivityCommand implements ICommand {
    @Override
    public SlashCommandData registerCommand() {
        return makeCommand();
    }

    @Override
    public String getDescription() {
        return "Get Recent Server Activity";
    }

    @Override
    public String getName() {
        return "activity";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        StringBuilder answer = new StringBuilder();
        ServerList.get().forEachServer(server -> {
            answer.append(server.getName());
            answer.append("\n");
            answer.append(server.sendToServer("cu activity", "spark tps"));
            answer.append("\n");
        });
        event.getHook().sendMessage(answer.toString()).setEphemeral(true).queue();
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}

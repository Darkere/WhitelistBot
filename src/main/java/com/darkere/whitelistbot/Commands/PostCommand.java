package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Util;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PostCommand implements ICommand{
    @Override
    public SlashCommandData registerCommand() {
        return makeCommand();
    }

    @Override
    public String getDescription() {
        return "Post the whitelist status to the info channel";
    }

    @Override
    public String getName() {
        return "post";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if(Util.updateStatusMessage(event.getJDA()))
        {
            event.getHook().sendMessage("Info Channel not found or unable to send to channel").queue();
            return;
        }
        event.getHook().sendMessage("Message changed!").queue();
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}

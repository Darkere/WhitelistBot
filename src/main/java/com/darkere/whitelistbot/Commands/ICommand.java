package com.darkere.whitelistbot.Commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommand {
    enum PermissionLevel{
        EVERYONE,
        MODERATOR
    }
    SlashCommandData registerCommand();
    String getDescription();

    String getName();

    default SlashCommandData makeCommand(){
        return Commands.slash(getName(),getDescription());
    }
    void execute(SlashCommandInteractionEvent event);
    PermissionLevel getRequiredPermissions();
}

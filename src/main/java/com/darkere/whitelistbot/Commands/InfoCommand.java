package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.UserDataHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class InfoCommand implements ICommand {
    @Override
    public SlashCommandData registerCommand() {
        return makeCommand()
            .addSubcommands(
                new SubcommandData("minecraft", "Info about Minecraft User")
                    .addOption(OptionType.STRING, "name", "Minecraft Username", true)
                    .addOptions(CommandFunctions.getShareOption()),
                new SubcommandData("discord", "Info about Discord User")
                    .addOption(OptionType.USER, "name", "Discord Username", true, false)
                    .addOptions(CommandFunctions.getShareOption())
            );
    }

    @Override
    public String getDescription() {
        return "Gets Info about a user";
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if(event.getSubcommandName() == null)
            return;
        if (event.getSubcommandName().equals("discord")) {
            String data = UserDataHandler.getUserData(event.getOption("name").getAsUser().getIdLong());
            if (data.isEmpty()) {
                event.getHook().sendMessage("User not found!").setEphemeral(CommandFunctions.getShareResult(event)).queue();
                return;
            }
            event.getHook().sendMessage(data).setEphemeral(CommandFunctions.getShareResult(event)).queue();
        } else if (event.getSubcommandName().equals("minecraft")) {
            String data = UserDataHandler.getUserData(event.getOption("name").getAsString());
            if (data.isEmpty()) {
                event.getHook().sendMessage("User not found!").setEphemeral(CommandFunctions.getShareResult(event)).queue();
                return;
            }
            event.getHook().sendMessage(data).setEphemeral(CommandFunctions.getShareResult(event)).queue();
        }
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}


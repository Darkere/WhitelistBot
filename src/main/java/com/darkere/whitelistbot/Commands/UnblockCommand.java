package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.UserDataHandler;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class UnblockCommand implements ICommand {

    @Override
    public SlashCommandData registerCommand() {
        return makeCommand()
            .addOption(OptionType.USER, "name", "Discord Username", true, false)
            .addOptions(CommandFunctions.getServerChoice(null).setRequired(false));
    }

    @Override
    public String getDescription() {
        return "Unblock a User, optionally only from one server";
    }

    @Override
    public String getName() {
        return "unblock";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Server server = CommandFunctions.getServer(event);
        User user = event.getOptionsByType(OptionType.USER).get(0).getAsUser();
        boolean unbl = UserDataHandler.unblockUser(user.getIdLong(), server);
        event.getHook().sendMessage(unbl ? "User has been unblocked" : "User was not found").queue();
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}

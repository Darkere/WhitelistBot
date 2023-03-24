package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.WhitelistBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class SetWhitelistCommand implements ICommand{
    @Override
    public SlashCommandData registerCommand() {
        return makeCommand()
            .addOptions(CommandFunctions.getServerChoice(server -> true))
            .addOption(OptionType.BOOLEAN,"open","true to open whitelist, false to close", true);
    }

    @Override
    public String getDescription() {
        return "set whitelist open or closed";
    }

    @Override
    public String getName() {
        return "setwhitelist";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Server server = CommandFunctions.getServer(event);
        server.setWhitelist(event.getOption("open").getAsBoolean());
        WhitelistBot.UpdateWhitelistCommand();
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.MODERATOR;
    }
}

package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Config.Config;
import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.UserDataHandler;
import com.darkere.whitelistbot.Util;
import com.darkere.whitelistbot.WhitelistBot;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class WhitelistCommand implements ICommand {

    @Override
    public SlashCommandData registerCommand() {
        return makeCommand()
            .addOptions(CommandFunctions.getServerChoice(Server::isWhitelistOpen))
            .addOption(OptionType.STRING, "name", "Enter your minecraft username", true)
            .addOption(OptionType.INTEGER, "age", "Enter your Age", true)
            .addOption(OptionType.STRING, "intro", "Introduce yourself. Only moderators may see this", true);
    }

    @Override
    public String getDescription() {
        return "Apply for Whitelist";
    }

    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        var server = CommandFunctions.getServer(event);
        String mcName = event.getOption("name").getAsString();
        WhitelistBot.logger.info(user.getName()  + " is trying to apply with username " + mcName);
        if (!Util.checkIfUserExists(mcName)) {
            Util.sendWithLog(event.getHook().sendMessage("That Minecraft Username does not exist!" + " Your Message: " + event.getCommandString()).setEphemeral(true),"Failed because mc does not exist in database");
            return;
        }

        if (UserDataHandler.hasAlreadyApplied(user, server.getName())) {
            Util.sendWithLog(event.getHook().sendMessage("You may only apply to a server once!").setEphemeral(true),user," application denied due to already exists");
            return;
        } else {
            UserDataHandler.addApplication(user.getIdLong(), server.getName(), mcName, true, false);
        }

        //load whitelist in case server was off during init
        server.ensureWhitelistLoaded();
        if(server.isWhitelisted(mcName)) {
            Util.sendWithLog(event.getHook().sendMessage(server.getAlreadyWhitelistedText()).setEphemeral(true),user," application denied because " + mcName + " is already whitelisted");
            return;
        }

        int age = event.getOption("age").getAsInt();
        String intro = event.getOption("intro").getAsString();

        TextChannel channel = event.getGuild().getTextChannelById(Config.get().getChannelID());
        String avatarUrl = user.getAvatarUrl();
        List<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field("Minecraft Name: ", mcName, true));
        fields.add(new MessageEmbed.Field("Age: ", Integer.toString(age), true));
        fields.add(new MessageEmbed.Field("Server ",  server.getName()  , true));
        fields.add(new MessageEmbed.Field("Introduction: ", intro, false));

        MessageEmbed embed = new MessageEmbed("", "Application", user.getAsMention(), EmbedType.IMAGE, OffsetDateTime.now(), -1, null, null, null, null, null, new MessageEmbed.ImageInfo(avatarUrl, null, 50, 50), fields);

        channel.sendMessage("Application by " + user.getAsMention()).queue();
        MessageCreateAction messageAction = channel.sendMessageEmbeds(embed);
        messageAction.setActionRow(Button.success("Success", "Approve"), Button.danger("Failure", "Deny"));
        messageAction.queue();

        event.getHook().sendMessage("Your Application has been received. The Bot will message you when a moderator approves or denies your application").setEphemeral(true).queue();
    }

    @Override
    public PermissionLevel getRequiredPermissions() {
        return PermissionLevel.EVERYONE;
    }
}

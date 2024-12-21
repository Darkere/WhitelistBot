package com.darkere.whitelistbot;

import com.darkere.whitelistbot.Commands.CommandFunctions;
import com.darkere.whitelistbot.Commands.ICommand;
import com.darkere.whitelistbot.Config.Config;
import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.Server.ServerList;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import static com.darkere.whitelistbot.WhitelistBot.logger;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) return;
        event.deferReply(CommandFunctions.getShareResult(event)).queue(null, Throwable::printStackTrace);
        ICommand command = WhitelistBot.getCommand(event.getName());
        if (command == null)
            return;

        if (command.getRequiredPermissions() == ICommand.PermissionLevel.EVERYONE || command.getRequiredPermissions() == ICommand.PermissionLevel.MODERATOR && event.getMember().getRoles().contains(event.getGuild().getRoleById(Config.get().getModeratorID()))) {
            command.execute(event);
        } else {
            Util.sendWithLog(event.getHook().sendMessage("You do not have Permission to use this command").setEphemeral(true),event.getUser()," tried to use " + command.getName() + " but had no permission (misconfigured bot settings)");
        }

    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        ServerList.get().forEachServer(Server::Shutdown);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferReply(true).queue(null, Throwable::printStackTrace);
        MessageEmbed embed = event.getMessage().getEmbeds().stream().findFirst().get();

        String id = embed.getDescription();
        if(id == null) {
            logger.error("failed to find embed that was clicked... what?");
            return;
        }

        id = id.replace("<", "");
        id = id.replace("@", "");
        id = id.replace(">", "");
        event.getJDA().retrieveUserById(id).queue(userToWhitelist -> {
            String playerName = embed.getFields().get(0).getValue();
            String serverName = embed.getFields().get(2).getValue();
            Server server = ServerList.get().getServer(serverName);
            if (server == null) {
                Util.sendWithLog(event.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("No config for server was found")),"failed to find config for server");
                Util.send(event.reply("server not found"));
                return;
            }

            if (event.getComponentId().equals("Success")) {
                Role role = event.getGuild().getRoleById(Config.get().GetServerRole());
                if (role != null)
                    Util.sendWithLog(event.getGuild().addRoleToMember(userToWhitelist, role),"applied server member role to " + userToWhitelist.getName());

                String response = server.sendToServer("whitelist add " + playerName);
                if (response.startsWith("Added")) {
                    UserDataHandler.addApplication(userToWhitelist.getIdLong(), serverName, playerName, false, true);
                    server.addWhitelisted(playerName);
                    Util.sendWithLog(userToWhitelist.openPrivateChannel().flatMap(channel -> channel.sendMessage(server.getAcceptedText())),"sent approval message to " + userToWhitelist.getName());
                    MessageEmbed newEmbed = new MessageEmbed(embed.getUrl(), "Approved", embed.getDescription(), embed.getType(), embed.getTimestamp(), 0x00FF00, null, null, null, null, null, embed.getImage(), embed.getFields());
                    Util.sendWithLog(event.getHook().editMessageEmbedsById(event.getMessageId(),newEmbed).setComponents(Collections.emptyList()),event.getUser(),"approved application for " + userToWhitelist.getName());
                    Util.send(event.getHook().sendMessage("approved " + userToWhitelist.getName()));
                } else {
                    Util.sendWithLog(event.getHook().sendMessage("failed to add to whitelist server responded with " + response),"failed to add to whitelist server responded with" + response);
                }
            } else if (event.getComponentId().equals("Failure")) {
                UserDataHandler.addApplication(userToWhitelist.getIdLong(), serverName, playerName, false, false);
                Util.sendWithLog(userToWhitelist.openPrivateChannel().flatMap(channel -> channel.sendMessage(server.getRejectedText())),"sent deny message to " + userToWhitelist.getName());
                MessageEmbed newEmbed = new MessageEmbed(embed.getUrl(), "Denied", embed.getDescription(), embed.getType(), embed.getTimestamp(), 0xFF0000, null, null, null, null, null, embed.getImage(), embed.getFields());
                Util.sendWithLog(event.getHook().editMessageEmbedsById(event.getMessageId(),newEmbed).setComponents(Collections.emptyList()),event.getUser(),"denied " + userToWhitelist.getName()+ "'s Application");
                Util.send(event.getHook().sendMessage("denied " + userToWhitelist.getName()));
            }
        }, x -> {
            MessageEmbed newEmbed = new MessageEmbed(embed.getUrl(), "Discord User could not be found", embed.getDescription(), embed.getType(), embed.getTimestamp(), 0xFF0000, null, null, null, null, null, embed.getImage(), embed.getFields());
            Util.sendWithLog(event.getHook().editMessageEmbedsById(event.getMessageId(),newEmbed),"was unable to find discord user for application");
            Util.send(event.getHook().sendMessage("failed to find discord user"));
        });

    }
}

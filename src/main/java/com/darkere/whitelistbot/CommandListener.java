package com.darkere.whitelistbot;

import com.darkere.whitelistbot.Commands.ICommand;
import com.darkere.whitelistbot.Config.Config;
import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.Server.ServerList;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) return;
        event.deferReply(true).queue();
        ICommand command = WhitelistBot.getCommand(event.getName());
        if (command == null)
            return;

        if (command.getRequiredPermissions() == ICommand.PermissionLevel.EVERYONE || command.getRequiredPermissions() == ICommand.PermissionLevel.MODERATOR && event.getMember().getRoles().contains(event.getGuild().getRoleById(Config.get().getModeratorID()))) {
            command.execute(event);
        } else {
            event.getHook().sendMessage("You do not have Permission to use this command").setEphemeral(true).queue();
        }

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        MessageEmbed embed = event.getMessage().getEmbeds().stream().findFirst().get();

        String id = embed.getDescription();
        id = id.replace("<", "");
        id = id.replace("@", "");
        id = id.replace(">", "");
        event.getJDA().retrieveUserById(id).queue(user -> {
            String playerName = embed.getFields().get(0).getValue();
            String serverName = embed.getFields().get(2).getValue();
            Server server = ServerList.get().getServer(serverName);
            if (server == null) {
                event.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("No config for server was found")).queue();
                return;
            }

            if (event.getComponentId().equals("Success")) {
                Role role = event.getGuild().getRoleById(Config.get().GetServerRole());
                if (role != null) event.getGuild().addRoleToMember(user, role).queue();

                String response = server.sendToServer("whitelist add " + playerName);
                if (response.startsWith("Added")) {
                    UserDataHandler.addApplication(user.getIdLong(), serverName, playerName, false, true);
                    server.addWhitelisted(playerName);
                    user.openPrivateChannel().flatMap(channel -> channel.sendMessage(server.getAcceptedText())).queue();
                    MessageEmbed newEmbed = new MessageEmbed(embed.getUrl(), "Approved", embed.getDescription(), embed.getType(), embed.getTimestamp(), 0x00FF00, null, null, null, null, null, embed.getImage(), embed.getFields());
                    event.editMessageEmbeds(newEmbed).setActionRows().queue();
                }
            } else if (event.getComponentId().equals("Failure")) {
                UserDataHandler.addApplication(user.getIdLong(), serverName, playerName, false, false);
                user.openPrivateChannel().flatMap(channel -> channel.sendMessage(server.getRejectedText())).queue();
                MessageEmbed newEmbed = new MessageEmbed(embed.getUrl(), "Denied", embed.getDescription(), embed.getType(), embed.getTimestamp(), 0xFF0000, null, null, null, null, null, embed.getImage(), embed.getFields());
                event.editMessageEmbeds(newEmbed).setActionRows().queue();
            }
        }, x -> {
            MessageEmbed newEmbed = new MessageEmbed(embed.getUrl(), "Discord User could not be found", embed.getDescription(), embed.getType(), embed.getTimestamp(), 0xFF0000, null, null, null, null, null, embed.getImage(), embed.getFields());
            event.editMessageEmbeds(newEmbed).queue();
        });

    }
}

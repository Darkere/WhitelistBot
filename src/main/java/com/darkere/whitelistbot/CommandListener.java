package com.darkere.whitelistbot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) return;
        event.deferReply(true).queue();

        if (event.getMember().getRoles().contains(event.getGuild().getRoleById(WhitelistBot.CurrentConfig.ModeratorRole))) {
            if (event.getName().equals("reload")) {
                Util.loadConfig();
                for (Server server : WhitelistBot.CurrentConfig.Servers) {
                    Util.loadWhitelist(server);
                }
                event.getHook().sendMessage("Reloaded").queue();
                return;
            }
            if (event.getName().equals("unblock")) {
                String sx = event.getOption("server").getAsString();
                User us = event.getOptionsByType(OptionType.USER).get(0).getAsUser();
                boolean unbl = UserDataHandler.unblockUser(us.getIdLong(), Util.getServerByName(sx).get());
                event.getHook().sendMessage(unbl ? "User has been unblocked" : "User was not found").queue();
                return;
            }
            if (event.getName().equals("setwhitelist")) {
                String serverX = event.getOption("server").getAsString();
                boolean open = event.getOption("open").getAsBoolean();
                Optional<Server> servern = Util.getServerByName(serverX);
                servern.ifPresent(s -> s.WhitelistOpen = open);
                Util.saveConfig();
                WebhookMessageAction<Message> restarting = event.getHook().sendMessage("Whitelist " + (open ? "opened" : "closed") +" Restarting").setEphemeral(true);
                AtomicBoolean wait = new AtomicBoolean(true);
                restarting.queue(x-> {
                    wait.set(false);
                });

                while (wait.get()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Util.updateStatusMessage(event.getJDA());
                WhitelistBot.restart();
                return;
            }
            if(event.getName().equals("restart")){
                WebhookMessageAction<Message> restarting = event.getHook().sendMessage("Restarting").setEphemeral(true);
                AtomicBoolean wait = new AtomicBoolean(true);
                restarting.queue(x-> {
                    wait.set(false);
                });

                while (wait.get()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                WhitelistBot.restart();
                return;
            }
            if (event.getName().equals("run")) {
                String server2 = event.getOption("server").getAsString();
                String c = event.getOption("command").getAsString();
                String an = Util.sendToServer(Util.getServerByName(server2).get(), c);
                event.getHook().sendMessage(an.isEmpty() ? "Offline" : an).setEphemeral(true).queue();
                return;
            }
            if (event.getName().equals("ping")) {
                OptionMapping map = event.getOption("server");
                StringBuilder answer = new StringBuilder();
                if (map != null) {
                    Optional<Server> o = Util.getServerByName(map.getAsString());
                    answer.append(Util.sendToServer(o.get(), "list"));
                } else {
                    for (Server server : WhitelistBot.CurrentConfig.Servers) {
                        answer.append(server.Name);
                        answer.append(": ");
                        answer.append(Util.sendToServer(server, "list"));
                        if (server.Name.equals("E2E"))
                            answer.append("\n");
                    }
                }
                if (answer.isEmpty())
                    answer.append("No Response.... Offline?");
                event.getHook().sendMessage(answer.toString()).setEphemeral(true).queue();
                return;
            }
            if (event.getName().equals("activity")) {
                StringBuilder answer = new StringBuilder();
                for (Server server : WhitelistBot.CurrentConfig.Servers) {
                    answer.append(server.Name);
                    answer.append("\n");
                    answer.append(Util.sendToServer(server, "cu activity","spark tps"));
                    answer.append("\n");
                }
                event.getHook().sendMessage(answer.toString()).setEphemeral(true).queue();
                return;
            }
            if (event.getName().equals("infomc")) {
                String data = UserDataHandler.getUserData(event.getOption("name").getAsString());
                OptionMapping op = event.getOption("share");
                boolean shared = op != null ? op.getAsBoolean(): false;
                if (data.isEmpty()){
                    event.getHook().sendMessage("User not found!").setEphemeral(shared).queue();
                    return;
                }
                event.getHook().sendMessage(data).setEphemeral(shared).queue();
                return;
            }
            if (event.getName().equals("info")) {
                String data = UserDataHandler.getUserData(event.getOption("name").getAsUser().getIdLong());
                OptionMapping op = event.getOption("share");
                boolean share = op != null ? op.getAsBoolean(): false;
                if (data.isEmpty()){
                    event.getHook().sendMessage("User not found!").setEphemeral(share).queue();
                    return;
                }
                event.getHook().sendMessage(data).setEphemeral(share).queue();
                return;
            }
            if(event.getName().equals("post")){

                if(Util.updateStatusMessage(event.getJDA()))
                {
                    event.getHook().sendMessage("Info Channel not found or unable to send to channel").queue();
                    return;
                }
                event.getHook().sendMessage("Message changed!").queue();
                return;
            }
        }
        if (event.getName().equals("whitelist")) {
            User user = event.getUser();
            String serverName = event.getOption("server").getAsString();
            String name = event.getOption("name").getAsString();

            if (!Util.checkIfUserExists(name)) {
                event.getHook().sendMessage("That Minecraft Username does not exist!" + " Your Message: " + event.getCommandString()).setEphemeral(true).queue();
            }

            if (UserDataHandler.hasAlreadyApplied(user.getIdLong(), serverName)) {
                event.getHook().sendMessage("You may only apply to a server once!").setEphemeral(true).queue();
                return;
            } else {
                UserDataHandler.addApplication(user.getIdLong(), serverName, name, true, false);
            }

            //load whitelist in case server was off during init
            if (!WhitelistBot.Whitelists.containsKey(serverName)) {
                Optional<Server> s = Util.getServerByName(serverName);
                s.ifPresent(Util::loadWhitelist);
            }

            if (!WhitelistBot.Whitelists.containsKey(serverName)) {
                event.getHook().sendMessage("Servers could not be reached to check for whitelist status, your application has been forwarded anyways").setEphemeral(true).queue();
            } else {
                Optional<Server> s = Util.getServerByName(serverName);
                if (WhitelistBot.Whitelists.get(serverName).contains(name) && s.isPresent()) {
                    String text = WhitelistBot.CurrentConfig.AlreadyWhitelistedText;
                    text = Util.parseText(text, s.get());
                    event.getHook().sendMessage(text).setEphemeral(true).queue();
                    return;
                }
            }
            int age = event.getOption("age").getAsInt();
            String intro = event.getOption("intro").getAsString();

            TextChannel channel = event.getGuild().getTextChannelById(WhitelistBot.CurrentConfig.ChannelID);
            String avatarUrl = user.getAvatarUrl();
            List<MessageEmbed.Field> fields = new ArrayList<>();
            fields.add(new MessageEmbed.Field("Minecraft Name: ", name, true));
            fields.add(new MessageEmbed.Field("Age: ", Integer.toString(age), true));
            fields.add(new MessageEmbed.Field("Server ", serverName, true));
            fields.add(new MessageEmbed.Field("Introduction: ", intro, false));

            MessageEmbed embed = new MessageEmbed("", "Application", user.getAsMention(), EmbedType.IMAGE, OffsetDateTime.now(), -1, null, null, null, null, null, new MessageEmbed.ImageInfo(avatarUrl, null, 50, 50), fields);

            MessageAction messageAction = channel.sendMessageEmbeds(embed);
            messageAction.setActionRow(Button.success("Success", "Approve"), Button.danger("Failure", "Deny"));
            messageAction.queue();

            event.getHook().sendMessage("Your Application has been received. The Bot will message you when a moderator approves or denies your application").setEphemeral(true).queue();
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
        event.getJDA().retrieveUserById(id).queue(user ->{
            String playerName = embed.getFields().get(0).getValue();
            String serverName = embed.getFields().get(2).getValue();
            Optional<Server> server = Util.getServerByName(serverName);
            if (server.isEmpty()) {
                event.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("No config for server was found")).queue();
                return;
            }

            if (event.getComponentId().equals("Success")) {
                Role role = event.getGuild().getRoleById(WhitelistBot.CurrentConfig.ServerRole);
                if (role != null) event.getGuild().addRoleToMember(user, role).queue();

                String response = Util.sendToServer(server.get(), "whitelist add " + playerName);
                if (!response.isEmpty()) {
                    UserDataHandler.addApplication(user.getIdLong(), serverName, playerName, false, true);
                    WhitelistBot.Whitelists.get(serverName).add(playerName);
                    String text = Util.parseText(WhitelistBot.CurrentConfig.AcceptedText, server.get());
                    user.openPrivateChannel().flatMap(channel -> channel.sendMessage(text)).queue();
                    MessageEmbed newEmbed = new MessageEmbed(embed.getUrl(), "Approved", embed.getDescription(), embed.getType(), embed.getTimestamp(), 0x00FF00, null, null, null, null, null, embed.getImage(), embed.getFields());
                    event.editMessageEmbeds(newEmbed).setActionRows().queue();
                }
            } else if (event.getComponentId().equals("Failure")) {
                UserDataHandler.addApplication(user.getIdLong(), serverName, playerName, false, false);
                String text = Util.parseText(WhitelistBot.CurrentConfig.RejectedText, server.get());
                user.openPrivateChannel().flatMap(channel -> channel.sendMessage(text)).queue();
                MessageEmbed newEmbed = new MessageEmbed(embed.getUrl(), "Denied", embed.getDescription(), embed.getType(), embed.getTimestamp(), 0xFF0000, null, null, null, null, null, embed.getImage(), embed.getFields());
                event.editMessageEmbeds(newEmbed).setActionRows().queue();
            }
        }, x ->{
            MessageEmbed newEmbed = new MessageEmbed(embed.getUrl(), "Discord User could not be found", embed.getDescription(), embed.getType(), embed.getTimestamp(), 0xFF0000, null, null, null, null, null, embed.getImage(), embed.getFields());
            event.editMessageEmbeds(newEmbed).queue();
        });

    }
}

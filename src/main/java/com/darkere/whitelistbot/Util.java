package com.darkere.whitelistbot;

import com.darkere.whitelistbot.Config.Config;
import com.darkere.whitelistbot.Config.ServerData;
import com.darkere.whitelistbot.Server.ServerList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.vv32.rcon.Rcon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


public class Util {

    private static final Map<String, Function<ServerData, String>> replacements = new HashMap<>();

    static {
        replacements.put("$servername", (server -> server.Name));
        replacements.put("$serverip", (server -> server.IP));
    }

    public static String parseText(String text, ServerData serverData) {
        for (Map.Entry<String, Function<ServerData, String>> entry : replacements.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue().apply(serverData));
        }
        return text;
    }

    public static boolean checkIfUserExists(String name) {
        try {
            URL url = new URL("https://playerdb.co/api/player/minecraft/" + name);
            URLConnection request = url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            return root.getAsJsonObject().get("success").getAsBoolean();
        } catch (IOException e) {
            System.out.println("Unable to Connect to Player Lookup");
        }

        return false;
    }
    public static boolean updateStatusMessage(JDA api) {
        TextChannel channel = api.getChannelById(TextChannel.class,Config.CONFIG.getInfoChannelID());
        if(channel == null)
            return false;
        AtomicBoolean failure = new AtomicBoolean(false);
        AtomicBoolean inProgress = new AtomicBoolean(true);
        channel.retrieveMessageById(Config.get().getInfoChannelMessageID()).queue(message -> {
            channel.editMessageEmbedsById(message.getIdLong(),createStatusEmbed()).queue(message1 -> inProgress.set(false));
        },y -> {
            channel.sendMessageEmbeds(createStatusEmbed()).queue(message -> {
                Config.get().setInfoChannelMesssageID(message.getIdLong());
                Config.get().saveConfig();
                inProgress.set(false);
            }, x -> {
                failure.set(true);
                inProgress.set(false);
            });
        });

        while (inProgress.get()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return failure.get();
    }
    public static MessageEmbed createStatusEmbed() {
        List<MessageEmbed.Field> fields = new ArrayList<>();
        ServerList.get().forEachServer(server -> {
            String ip = "```fix\n" + server.getIP() + "\n```";
            MessageEmbed.Field f = new MessageEmbed.Field((server.isWhitelistOpen() ? "OPEN" : "CLOSED") + " | " + server.getDisplayName() + " " + (server.getVersion() == null ? "" : server.getVersion()) ,ip,false);
            fields.add(f);
        });
        return new MessageEmbed("",
            "Whitelist application status for current servers:",
            "To Apply for whitelist use the **/whitelist** command!",
            EmbedType.IMAGE, OffsetDateTime.now(),
            0x000000,
            new MessageEmbed.Thumbnail("https://avatars.githubusercontent.com/u/77247973?s=200&v=4","",200,200),
            null,
            null,
            null,
            new MessageEmbed.Footer("All Servers are hosted in Central Europe","",""),
            null,
            fields);
    }
}

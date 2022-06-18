package com.darkere.whitelistbot;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
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

    private static final Map<String, Function<Server, String>> replacements = new HashMap<>();

    static {
        replacements.put("$servername", (server -> server.Name));
        replacements.put("$serverip", (server -> server.IP));
    }

    public static void loadConfig() {
        File file = new File("config.json");
        if (!file.exists()) {
            WhitelistBot.CurrentConfig = createExampleConfig(file);
        } else {
            try {
                String json = Files.readString(file.toPath());
                WhitelistBot.CurrentConfig = WhitelistBot.gson.fromJson(json, Config.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        UserDataHandler.loadUserData();
    }

    public static void saveConfig() {
        File file = new File("config.json");
        try {
            if (!file.exists())
                file.createNewFile();
            String json = WhitelistBot.gson.toJson(WhitelistBot.CurrentConfig);
            Files.writeString(file.toPath(), json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Config createExampleConfig(File file) {
        Config config = new Config();
        config.BotToken = "BOT TOKEN HERE";
        config.ChannelID = 123456789;
        config.GuildID = 123456789;
        config.ServerRole = 12346579;
        Server server = new Server();
        config.Servers = new ArrayList<>();
        config.Servers.add(server);
        server.IP = "enigmatica.net";
        server.Name = "E2E";
        server.RconPassword = "password required for whitelisting, set in server.properties";
        server.Rconport = "rcon port, found in server.properties";
        server.WhitelistOpen = true;
        config.AcceptedText = "Your application to join our $servername has been accepted. Check the #server-announcements channel for the current version of the pack. The current IP is $serverip";
        config.RejectedText = "Your application for our $servername server has been denied. Sorry.";
        config.AlreadyWhitelistedText = "You are already whitelisted on $servername. Still unable to join? Post in #support";

        try {
            if (file.createNewFile())

                Files.writeString(file.toPath(), WhitelistBot.gson.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    public static String sendToServer(Server server, String... command) {
        String rconIP = server.IP;
        if (rconIP.contains(":")) {
            rconIP = rconIP.split(":")[0];
        }
        try (Rcon rcon = Rcon.open(rconIP, Integer.parseInt(server.Rconport))) {
            if (rcon.authenticate(server.RconPassword)) {
                System.out.println("Connected to " + server.Name);
                StringBuilder builder = new StringBuilder();
                for (String s : command) {
                    builder.append(rcon.sendCommand(s));
                    if(command.length > 1)
                        builder.append("\n");
                }
                return builder.toString();
            } else {
                System.out.println("Failed to authenticate with " + server.Name);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to RCon for " + server.Name);
        }
        return "";
    }

    public static void loadWhitelist(Server server) {
        String response = sendToServer(server, "whitelist list");
        if (response.isEmpty()) return;
        String names = response.split(":")[1];
        Set<String> whitelists = new HashSet<>();
        for (String s : names.split(",")) {
            whitelists.add(s.trim());
        }
        WhitelistBot.Whitelists.put(server.Name, whitelists);
    }

    public static String parseText(String text, Server server) {
        for (Map.Entry<String, Function<Server, String>> entry : replacements.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue().apply(server));
        }
        return text;
    }

    public static Optional<Server> getServerByName(String name) {
        return WhitelistBot.CurrentConfig.Servers.stream().filter(s -> s.Name.equals(name)).findFirst();
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
        TextChannel channel = api.getChannelById(TextChannel.class,WhitelistBot.CurrentConfig.InfoChannelID);
        if(channel == null)
            return false;
        AtomicBoolean failure = new AtomicBoolean(false);
        AtomicBoolean inProgress = new AtomicBoolean(true);
        channel.retrieveMessageById(WhitelistBot.CurrentConfig.InfoChannelMessageID).queue(message -> {
            channel.editMessageEmbedsById(message.getIdLong(),createStatusEmbed()).queue(message1 -> inProgress.set(false));
        },y -> {
            channel.sendMessageEmbeds(createStatusEmbed()).queue(message -> {
                WhitelistBot.CurrentConfig.InfoChannelMessageID = message.getIdLong();
                saveConfig();
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
        WhitelistBot.CurrentConfig.Servers.forEach(server -> {
            String ip = "```fix\n" + server.IP + "\n```";
            MessageEmbed.Field f = new MessageEmbed.Field((server.WhitelistOpen ? "OPEN" : "CLOSED") + " | " + server.DisplayName,ip,false);
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

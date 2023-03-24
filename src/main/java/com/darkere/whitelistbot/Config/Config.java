package com.darkere.whitelistbot.Config;

import com.darkere.whitelistbot.Util;
import com.darkere.whitelistbot.WhitelistBot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class Config {

    public static Config CONFIG;
    public static Config get(){
        if(CONFIG == null) {
            CONFIG = new Config();
        }
        return CONFIG;
    }

    private ConfigData data;


    public void saveConfig() {
        File file = new File("config.json");
        try {
            if (!file.exists())
                file.createNewFile();
            String json = WhitelistBot.gson.toJson(data);
            Files.writeString(file.toPath(), json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void loadConfig() {
        File file = new File("config.json");
        if (!file.exists()) {
           data = createExampleConfig(file);
        } else {
            try {
                String json = Files.readString(file.toPath());
                data = WhitelistBot.gson.fromJson(json, ConfigData.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static ConfigData createExampleConfig(File file) {
        ConfigData config = new ConfigData();
        config.BotToken = "BOT TOKEN HERE";
        config.ChannelID = 123456789;
        config.GuildID = 123456789;
        config.ServerRole = 12346579;
        ServerData serverData = new ServerData();
        config.serverData = new ArrayList<>();
        config.serverData.add(serverData);
        serverData.IP = "enigmatica.net";
        serverData.Name = "E2E";
        serverData.RconPassword = "password required for whitelisting, set in server.properties";
        serverData.Rconport = "rcon port, found in server.properties";
        serverData.WhitelistOpen = true;
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

    public String getAlreadyWhitelistedText(){
        return data.AlreadyWhitelistedText;
    }
    public long getChannelID(){
        return data.ChannelID;
    }
    public long getInfoChannelID(){
        return data.InfoChannelID;
    }
    public long getInfoChannelMessageID(){
        return data.InfoChannelMessageID;
    }
    public void setInfoChannelMesssageID(long id){
        data.InfoChannelMessageID = id;
    }

    public long GetServerRole() {
        return data.ServerRole;
    }

    public String getToken() {
        return data.BotToken;
    }

    public long getGuildID() {
        return data.GuildID;
    }

    public long getModeratorID() {
        return data.ModeratorRole;
    }

    public String getRejectedText() {
        return data.RejectedText;
    }

    public String getAcceptedText() {
        return data.AcceptedText;
    }
}


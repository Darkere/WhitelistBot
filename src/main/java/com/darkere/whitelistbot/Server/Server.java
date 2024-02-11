package com.darkere.whitelistbot.Server;

import com.darkere.whitelistbot.Config.Config;
import com.darkere.whitelistbot.Config.ServerData;
import com.darkere.whitelistbot.Util;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import nl.vv32.rcon.Rcon;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Server {


    Set<String> whitelist = new HashSet<>();

    public ServerData data;

    public String getName() {
        return data.Name;
    }

    public void ensureWhitelistLoaded() {
        if (whitelist.isEmpty())
            loadWhitelist();
    }

    public String sendToServer(String... command) {
        String rconIP = data.IP;
        if (rconIP.contains(":")) {
            rconIP = rconIP.split(":")[0];
        }
        try (Rcon rcon = Rcon.open(rconIP, Integer.parseInt(data.Rconport))) {
            if (rcon.authenticate(data.RconPassword)) {
                System.out.println("Connected to " + data.Name);
                StringBuilder builder = new StringBuilder();
                for (String s : command) {
                    builder.append(rcon.sendCommand(s));
                    if (command.length > 1)
                        builder.append("\n");
                }
                return builder.toString();
            } else {
                System.out.println("Failed to authenticate with " + data.Name);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to RCon for " + data.Name);
        }
        return "";
    }

    public void loadWhitelist() {
        String response = sendToServer("whitelist list");
        if (response.isEmpty()) return;
        String names = response.split(":")[1];
        whitelist.clear();
        for (String s : names.split(",")) {
            whitelist.add(s.trim());
        }
    }

    public String getAlreadyWhitelistedText() {
        String text = Config.get().getAlreadyWhitelistedText();
        return Util.parseText(text, data);
    }

    public boolean isWhitelisted(String mcName) {
        return whitelist.contains(mcName);
    }

    public boolean isWhitelistOpen() {
        return data.WhitelistOpen;
    }

    public void setWhitelist(boolean open) {
        data.WhitelistOpen = open;
        Config.get().saveConfig();
    }

    public String getIP() {
        return data.IP;
    }

    public String getDisplayName() {
        return data.DisplayName;
    }

    public String getVersion() {
        return data.Version;
    }

    public void setVersion(String version) {
        data.Version = version;
        Config.get().saveConfig();
    }

    public String getRejectedText() {
        String text = Config.get().getRejectedText();
        return Util.parseText(text, data);
    }

    public String getAcceptedText() {
        String text = Config.get().getAcceptedText();
        return Util.parseText(text, data);
    }

    public void addWhitelisted(String playerName) {
        whitelist.add(playerName);
    }
}

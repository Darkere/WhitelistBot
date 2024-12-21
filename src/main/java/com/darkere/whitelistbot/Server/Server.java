package com.darkere.whitelistbot.Server;

import com.darkere.whitelistbot.Config.Config;
import com.darkere.whitelistbot.Config.ServerData;
import com.darkere.whitelistbot.Util;
import nl.vv32.rcon.Rcon;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.util.HashSet;
import java.util.Set;

import static com.darkere.whitelistbot.WhitelistBot.logger;

public class Server {


    Set<String> whitelist = new HashSet<>();

    public ServerData data;

    public String getName() {
        return data.Name;
    }
    boolean connected = false;
    Rcon rcon;

    public void ensureWhitelistLoaded() {
        if (whitelist.isEmpty())
            loadWhitelist();
    }
    private boolean connectToServer() {
        String rconIP = data.IP;
        if (rconIP.contains(":")) {
            rconIP = rconIP.split(":")[0];
        }
        try {
            rcon = Rcon.open(rconIP, Integer.parseInt(data.Rconport));
            if (rcon.authenticate(data.RconPassword)) {
                logger.info("Connected to " + data.Name);
                connected = true;
                return true;
            } else {
                logger.error("Failed to authenticate with " + data.Name);
            }
        } catch (IOException | UnresolvedAddressException e) {
            logger.debug("Unable to connect to RCon for " + data.Name + " at " + rconIP + ":" + data.Rconport,e);
            logger.error("Unable to connect to RCon for " + data.Name + " at " + rconIP + ":" + data.Rconport + " " + e.getClass());
        }
        return false;
    }
    public String sendToServer(String command) {
        return sendToServer(command, false);
    }
    public String sendToServer(String command, boolean retry) {
       if(!connected)
           if(!connectToServer())
               return "";

        try {
            return rcon.sendCommand(command);
        } catch (IOException e) {
            if(retry)
                return "";
            connected = false;
            sendToServer(command,true);
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

    public void Shutdown() {
        try {
            rcon.close();
        } catch (IOException e) {
            logger.error("error shutting down ",e);
        }
    }
}

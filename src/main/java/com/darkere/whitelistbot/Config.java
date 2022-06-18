package com.darkere.whitelistbot;

import java.util.List;
import java.util.Set;

public class Config {

    long GuildID;
    String BotToken;
    long ChannelID;

    long InfoChannelID;

    long InfoChannelMessageID;
    long ServerRole;

    long ModeratorRole;
    List<Server> Servers;

    String AcceptedText;
    String RejectedText;
    String AlreadyWhitelistedText;
}

class Server {
    String Name;

    String DisplayName;
    String IP;
    String Rconport;
    String RconPassword;

    boolean WhitelistOpen;
}
class UserData {
    long id;
    String MCUsername;
    Set<String> AppliedServers;
    Set<String> AcceptedServers;
    Set<String> DeniedServers;

}
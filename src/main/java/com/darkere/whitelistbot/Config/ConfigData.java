package com.darkere.whitelistbot.Config;

import java.util.List;

public class ConfigData {
    public long GuildID;
    public String BotToken;
    public long ChannelID;

    public long InfoChannelID;

    public long InfoChannelMessageID;
    public long ServerRole;

    public long ModeratorRole;
    public List<ServerData> serverData;

    public String AcceptedText;
    public String RejectedText;
    public String AlreadyWhitelistedText;
}

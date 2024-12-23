package com.darkere.whitelistbot;

import com.darkere.whitelistbot.Commands.*;
import com.darkere.whitelistbot.Config.Config;
import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.Server.ServerList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.*;


public class WhitelistBot {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final List<ICommand> Commands = List.of(new ActivityCommand(),
            new InfoCommand(),
            new PingCommand(),
            new PostCommand(),
            new ReloadConfigCommand(),
            new RunCommand(),
            new SetVersionCommand(),
            new SetWhitelistCommand(),
            new UnblockCommand(),
            new WhitelistCommand());
    private static JDA api = null;
    public static final Logger logger = LoggerFactory.getLogger("WhitelistBot");
    public static void main(String[] args) {
        if(api != null) {
            api.shutdownNow();
            api = null;
        }

        Config.get().loadConfig();
        ServerList.get().init();
        try {
            api = JDABuilder.createDefault(Config.get().getToken()).build().awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        api.addEventListener(new CommandListener());
        Guild guild = api.getGuildById(Config.get().getGuildID());
        if(guild == null) {
           logger.error("Guild not found");
            return;
        }
        for (ICommand command : Commands) {
            Util.sendWithLog(guild.upsertCommand(command.registerCommand())," registering command " +command.getName());
        }
        ServerList.get().forEachServer(Server::loadWhitelist);
    }

    public static void UpdateWhitelistCommand() {
        var whitelistCommand = getCommand("whitelist");
        api.getGuildById(Config.get().getGuildID()).upsertCommand(whitelistCommand.registerCommand());
    }
    public static ICommand getCommand(String name){
        return Commands.stream().filter(command -> command.getName().equals(name)).findAny().orElse(null);
    }
}

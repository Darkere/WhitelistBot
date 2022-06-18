package com.darkere.whitelistbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import javax.security.auth.login.LoginException;
import java.util.*;


public class WhitelistBot {

    public static Config CurrentConfig;
    public static Map<String, Set<String>> Whitelists = new HashMap<>();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static List<UserData> BLOCKED_USERS = new ArrayList<>();
    private static JDA api = null;

    public static void main(String[] args) {
       restart();
    }
    public static void restart(){
        if(api != null) {
            api.shutdownNow();
            api = null;
        }

        Util.loadConfig();
        try {
            api = JDABuilder.createDefault(CurrentConfig.BotToken).build().awaitReady();
        } catch (InterruptedException | LoginException e) {
            throw new RuntimeException(e);
        }

        api.addEventListener(new CommandListener());

        Guild guild = api.getGuildById(CurrentConfig.GuildID);
        if(guild == null) {
            System.out.println("Guild not found");
            return;
        }
        OptionData serverChoice = new OptionData(OptionType.STRING, "server", "Server you are applying for", true);
        CurrentConfig.Servers.forEach(server -> serverChoice.addChoice(server.Name, server.Name));
        OptionData serverChoiceWhitelist = new OptionData(OptionType.STRING, "server", "Server you are applying for", true);
        CurrentConfig.Servers.forEach(server -> {
            if(server.WhitelistOpen)
                serverChoiceWhitelist.addChoice(server.Name, server.Name);
        } );
        OptionData serverChoiceNotRequired = new OptionData(OptionType.STRING, "server", "Server you are applying for", false);
        CurrentConfig.Servers.forEach(server -> serverChoiceNotRequired.addChoice(server.Name, server.Name));
        CommandListUpdateAction commands = guild.updateCommands();
        commands.addCommands(
            Commands.slash("whitelist", "Apply to be whitelisted on our servers")
                .addOptions(serverChoiceWhitelist)
                .addOption(OptionType.STRING, "name", "Enter your minecraft username", true)
                .addOption(OptionType.INTEGER, "age", "Enter your Age", true)
                .addOption(OptionType.STRING, "intro", "Introduce yourself. Only moderators may see this", true),
            Commands.slash("reload","reload the config"),
            Commands.slash("infomc","Get User Info" )
                .addOption(OptionType.STRING, "name", "Minecraft User Name", true)
                .addOption(OptionType.BOOLEAN, "share", "Share in Channel"),
            Commands.slash("info", "Get User info")
                .addOption(OptionType.USER, "name", "Discord User", true)
                .addOption(OptionType.BOOLEAN, "share", "Share in Channel"),
            Commands.slash("ping","test if servers work")
                .addOptions(serverChoiceNotRequired),
            Commands.slash("run","run command on server")
                .addOptions(serverChoice)
                .addOption(OptionType.STRING,"command","Command to run", true),
            Commands.slash("activity", "check recent server activity"),
            Commands.slash("setwhitelist", "set whitelist open or closed")
                .addOptions(serverChoice)
                .addOption(OptionType.BOOLEAN,"open","true to open whitelist, false to close", true),
            Commands.slash("unblock", "Allow a user to reapply to a server")
                .addOption(OptionType.USER, "name", "Discord User", true)
                .addOptions(serverChoice),
            Commands.slash("restart", "Restarts the bot"),
            Commands.slash("post", "Post the whitelist status to the info channel")
        ).queue();


        for (Server server : CurrentConfig.Servers) {
            Util.loadWhitelist(server);
        }
    }
}

package com.darkere.whitelistbot.Commands;

import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.Server.ServerList;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.function.Predicate;


public class CommandFunctions {


    static OptionData getServerChoice(Predicate<Server> select) {
        OptionData option = new OptionData(OptionType.STRING, "server", "Server you are applying for", true);
        ServerList.get().forEachServer(server -> {
            if (select == null || select.test(server)) option.addChoice(server.data.DisplayName, server.data.Name);
        });
        return option;
    }

    static Server getServer(SlashCommandInteractionEvent event) {
        String name = event.getOption("server").getAsString();
        return ServerList.get().getServer(name);
    }
    static OptionData getShareOption(){
        return new OptionData(OptionType.BOOLEAN, "share", "Share in Channel",false);
    }

    public static boolean getShareResult(SlashCommandInteractionEvent event) {
        OptionMapping op = event.getOption("share");
        return op == null || !op.getAsBoolean();
    }
}

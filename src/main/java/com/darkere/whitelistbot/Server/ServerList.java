package com.darkere.whitelistbot.Server;

import com.darkere.whitelistbot.Config.Config;
import com.darkere.whitelistbot.Config.ServerData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.darkere.whitelistbot.WhitelistBot.logger;

public class ServerList {
    private static ServerList INSTANCE;
    private List<Server> list = new ArrayList<>();
    public static ServerList get(){
        if(INSTANCE == null){
            INSTANCE = new ServerList();
        }
        return INSTANCE;
    }

    public Server getServer(String name) {
         var s = list.stream().filter(server -> server.data.Name.equals(name)).findFirst();
         return s.orElse(null);
    }

    public void forEachServer(Consumer<Server> Consumer){
        for (Server server : list) {
            Consumer.accept(server);
        }
    }

    public void init() {
        for (ServerData sdata : Config.get().GetServerData()) {
            var server = new Server();
            server.data = sdata;
            logger.info("Loaded Data for "+ sdata.DisplayName);
            list.add(server);
        }
    }
}

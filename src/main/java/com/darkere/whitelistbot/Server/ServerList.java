package com.darkere.whitelistbot.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

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
}

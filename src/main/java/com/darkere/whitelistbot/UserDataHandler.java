package com.darkere.whitelistbot;

import com.darkere.whitelistbot.Config.ServerData;
import com.darkere.whitelistbot.Config.UserData;
import com.darkere.whitelistbot.Server.Server;
import com.darkere.whitelistbot.Server.ServerList;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class UserDataHandler {

    private static final String USER_DATA_FILE = "UserData.json";
    public static List<UserData> BLOCKED_USERS = new ArrayList<>();
    public static void loadUserData(){
        File file = new File(USER_DATA_FILE);
        if(!file.exists())
            return;
        try {
            String json = Files.readString(file.toPath());
            BLOCKED_USERS = WhitelistBot.gson.fromJson(json,new TypeToken<ArrayList<UserData>>(){}.getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean unblockUser(long id, Server server){
        Optional<UserData> user = BLOCKED_USERS.stream().filter(u -> u.id == id).findFirst();
        user.ifPresent(us -> {
            List<Server> servers = new ArrayList<>();
            if(server != null)
                servers.add(server);
            else
                ServerList.get().forEachServer(servers::add);
            for (Server serverLoop : servers) {
                us.AppliedServers.remove(serverLoop.getName());
                us.DeniedServers.remove(serverLoop.getName());
                us.AcceptedServers.remove(serverLoop.getName());
            }
            saveUserData();
        });
        return user.isPresent();
    }

    public static String getUserData(long id){
        Optional<UserData> user = BLOCKED_USERS.stream().filter(u -> u.id == id).findFirst();
        if(user.isEmpty())
            return "";
        return WhitelistBot.gson.toJson(user.get());
    }

    public static String getUserData(String name){
        Optional<UserData> user = BLOCKED_USERS.stream().filter(u -> u.MCUsername == name).findFirst();
        if(user.isEmpty())
            return "";
        return WhitelistBot.gson.toJson(user.get());
    }

    public static boolean hasAlreadyApplied(User user, String server){
        Optional<UserData> FoundUser = BLOCKED_USERS.stream().filter(u -> u.id == user.getIdLong()).findFirst();
        if(FoundUser.isEmpty())
            return false;
        var found = FoundUser.get();
        if(found.AppliedServers.contains(server)){
            WhitelistBot.logger.info(user.getName() + " has already applied to " + server + " under the name " + found.MCUsername +  "! Used /unblock if they should be allowed to apply again");
            return true;
        }
        return false;
    }

    public static void addApplication(long id, String server, String MCName, boolean apply, boolean approved) {
        Optional<UserData> user = BLOCKED_USERS.stream().filter(u -> u.id == id).findFirst();
        user.ifPresentOrElse(us -> {
            if(apply)
                us.AppliedServers.add(server);
            else if(approved)
                us.AcceptedServers.add(server);
            else
                us.DeniedServers.add(server);
            saveUserData();
        },() -> {
            UserData data = new UserData();
            data.id = id;
            data.MCUsername = MCName;
            data.AppliedServers = new HashSet<>();
            data.AppliedServers.add(server);
            data.AcceptedServers = new HashSet<>();
            data.DeniedServers = new HashSet<>();
            BLOCKED_USERS.add(data);
            saveUserData();
        });
    }

    public static void saveUserData(){
        File file = new File(USER_DATA_FILE);
        try {
            if(!file.exists())
                file.createNewFile();
            String json = WhitelistBot.gson.toJson(BLOCKED_USERS);
            Files.writeString(file.toPath(),json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

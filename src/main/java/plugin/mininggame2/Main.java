package plugin.mininggame2;

import java.net.http.WebSocket.Listener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.mininggame2.command.MiningGameCommand;

public final class Main extends JavaPlugin implements Listener, org.bukkit.event.Listener {

    @Override
    public void onEnable() {
        MiningGameCommand miningGameCommand = new MiningGameCommand(this);
        Bukkit.getPluginManager().registerEvents(miningGameCommand ,this);
        getCommand("miningGame").setExecutor(miningGameCommand);

    }


}

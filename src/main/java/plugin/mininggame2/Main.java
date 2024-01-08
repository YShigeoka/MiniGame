package plugin.mininggame2;

import java.net.http.WebSocket.Listener;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.mininggame2.command.GameCancelCommand;
import plugin.mininggame2.command.MiningGameCommand;


public final class Main extends JavaPlugin implements Listener, org.bukkit.event.Listener {


    @Override
    public void onEnable() {
        MiningGameCommand miningGameCommand = new MiningGameCommand(this);
        Bukkit.getPluginManager().registerEvents(miningGameCommand ,this);
        Objects.requireNonNull(getCommand("miningGame")).setExecutor(miningGameCommand);

        GameCancelCommand gameCancelCommand = new GameCancelCommand(this, miningGameCommand);
        Objects.requireNonNull(getCommand("gameCancel")).setExecutor(gameCancelCommand);
    }
}

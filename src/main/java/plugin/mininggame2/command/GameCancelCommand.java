package plugin.mininggame2.command;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.mininggame2.Main;
import plugin.mininggame2.data.ExecutingPlayer;

public class GameCancelCommand extends BaseCommand {

  private final Main main;
  private final  MiningGameCommand miningGameCommand;

  public GameCancelCommand (Main main, MiningGameCommand miningGameCommand) {
    this.main = main;
    this.miningGameCommand = miningGameCommand;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
    List<ExecutingPlayer> executingPlayerList = miningGameCommand.executingPlayerList;
    boolean executingPlayer = executingPlayerList
        .stream()
        .anyMatch(p -> p.getPlayerName().equals(player.getName()));

    if(executingPlayer){
      Bukkit.getScheduler().cancelTasks(main);
      executingPlayerList.clear();
      player.sendMessage(ChatColor.RED +"ゲームが強制終了されました。");

    } else {
      player.sendMessage("ゲームは実行されていません。");
    }
    return false;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label,
      String[] args) {
    return false;
  }
}

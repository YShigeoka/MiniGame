package plugin.mininggame2.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * コマンドを実行して動かす基底クラスです。
 */
public abstract class BaseCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      return onExecutePlayerCommand(player);
    }else {
      return onExecuteNPCCommand(sender);
    }
  }

  /**
   * コマンド実行者がプレーヤーの場合に実行される。
   * @param player コマンド実行プレーヤー
   * @return　処理の実行有無
   */
  public abstract boolean onExecutePlayerCommand(Player player);

  /**
   * コマンド実行者がプレーヤー以外の場合に実行される。
   * @param sender　コマンド実行者
   * @return　処理の実行有無
   */
  public abstract boolean onExecuteNPCCommand(CommandSender sender);
}

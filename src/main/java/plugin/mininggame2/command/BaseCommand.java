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
      return onExecutePlayerCommand(player, command, label, args);
    }else {
      return onExecuteNPCCommand(sender, command, label, args);
    }
  }

  /**
   * コマンド実行者がプレーヤーの場合に実行される。
   * @param player コマンド実行プレーヤー
   * @param command コマンド
   * @param label　ラベル
   * @param args　コマンド引数
   * @return　処理の実行有無
   */
  public abstract boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args);

  /**
   * コマンド実行者がプレーヤー以外の場合に実行される。
   * @param sender　コマンド実行者
   * @return　処理の実行有無
   */
  public abstract boolean onExecuteNPCCommand(CommandSender sender, Command command, String label, String[] args);
}

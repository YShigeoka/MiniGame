package plugin.mininggame2.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import plugin.mininggame2.Main;
import plugin.mininggame2.data.PlayerScore;

public class miningGameCommand implements Listener, CommandExecutor{

  private Main main;
  private List<PlayerScore> playerScoreList = new ArrayList<>();
  private List<Block> generatedBlocks = new ArrayList<>();

  public miningGameCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      PlayerScore nowPlayer = getPlayerScore(player);
      nowPlayer.setGameTime(20);

      World world = player.getWorld();

      //プレーヤーの初期ステータス設定（体力・空腹値・装備）
      initPlayerStatus(player);

      Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
        if (nowPlayer.getGameTime() <= 0) {
          Runnable.cancel();
          player.sendTitle("ゲーム終了！",
              nowPlayer.getPlayerName() + " 合計 " + nowPlayer.getScore() + "点！",
              5, 40, 5);
          nowPlayer.setScore(0);

          for (Block block : generatedBlocks) {
            block.setType(Material.AIR);
          }
          generatedBlocks.clear();

          return;
        }
        nowPlayer.setGameTime(nowPlayer.getGameTime() - 20);
      }, 0, 30 * 20);

    }
    return false;
  }


  /**
   * 新規のプレーヤー情報をリストに追加する。
   *
   * @param player 　コマンドを実行したプレーヤー。
   * @return 新規プレーヤー
   */
  private PlayerScore addNwePlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
    return newPlayer;
  }


  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    // ブロックを採掘したプレーヤーを取得
    Player player = e.getPlayer();

    if (Objects.isNull(player) || playerScoreList.isEmpty()) {
      return;
    }

    // 採掘されたブロックの種類を取得
    Material blockType = e.getBlock().getType();

    for (PlayerScore playerScore : playerScoreList) {
      if (playerScore.getPlayerName().equals(player.getName())) {
        int nowScore = playerScore.getScore();
        int scoreIncrement = 0;

        switch (blockType) {
          case COAL_ORE -> scoreIncrement = 2;
          case IRON_ORE -> scoreIncrement = 5;
          case DIAMOND_ORE -> scoreIncrement = 10;
          case LAPIS_ORE -> scoreIncrement = 15;
          case EMERALD_ORE -> scoreIncrement = 20;
        }

        playerScore.setScore(nowScore + scoreIncrement);

        if (scoreIncrement > 0) {
          player.sendMessage("採掘ポイント獲得！現在のスコアは" + playerScore.getScore() + "点");
        }
      }
    }
  }

  /**
   * 現在実行しているプレーヤーのスコア情報を取得する。
   *
   * @param player 　コマンド実行プレーヤー
   * @return　現在実行しているプレーヤーのスコア情報
   */
  private PlayerScore getPlayerScore(Player player) {
    if (playerScoreList.isEmpty()) {
      return addNwePlayer(player);
    } else {
      for (PlayerScore playerScore : playerScoreList) {
        if (!playerScore.getPlayerName().equals(player.getName())) {
          return addNwePlayer(player);
        } else {
          return playerScore;
        }
      }
    }
    return null;
  }


  /**
   * ゲーム開始時のプレーヤーの状態の設定 体力・空腹値を20(最大）にし、右手装備をダイヤモンドピッケルにする
   *
   * @param player 　コマンド実行プレーヤー
   */
  private void initPlayerStatus(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);
    player.sendTitle("鉱石採掘ゲーム　スタート！", "", 5, 40, 5);

    PlayerInventory inventory = player.getInventory();
    inventory.setItemInMainHand(new ItemStack(Material.DIAMOND_PICKAXE));

    World world = player.getWorld();
    Location playerLocation = player.getLocation();

    double x = playerLocation.getX() + 7;
    double y = playerLocation.getY() + 3;//sizeの半数値で地上に生成
    double z = playerLocation.getZ() ;
    // 新しい範囲の中心座標
    Location centerLocation = new Location(playerLocation.getWorld(), x, y, z);

    // 新しい範囲を生成（サイズ5）
    generateRandomBlocks(centerLocation, 7);
  }

  /**
   * @param center
   * @param size
   */
  private List<Block> generateRandomBlocks(Location center, int size) {
    World world = center.getWorld();

    for (int x = -size / 2; x <= size / 2; x++) {
      for (int y = -size / 2; y <= size / 2; y++) {
        for (int z = -size / 2; z <= size / 2; z++) {
          Location blockLocation = new Location(world, center.getX() + x, center.getY() + y,
              center.getZ() + z);
          Block block = world.getBlockAt(blockLocation);

          // ランダムなブロックの種類を選択
          Material blockType = getRandomBlockType();
          block.setType(blockType);
          generatedBlocks.add(block);
        }
      }
    }
    return generatedBlocks;
  }

  /**
   *
   * @return
   */
  private Material getRandomBlockType() {
    int random = new SplittableRandom().nextInt(100);

    if (random >= 84) {
      if (random < 85) {
        return Material.COAL_ORE;
      } else if (random < 90) {
        return Material.IRON_ORE;
      } else if (random < 96) {
        return Material.DIAMOND_ORE;
      } else if (random < 98) {
        return Material.LAPIS_ORE;
      } else {
        return Material.EMERALD_ORE;
      }
    } else {
      return Material.STONE;
    }
  }
}



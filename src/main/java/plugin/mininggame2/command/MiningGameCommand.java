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
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import plugin.mininggame2.Main;
import plugin.mininggame2.data.PlayerScore;

/**
 * 制限時間内にランダムで出現する鉱石（ブロック）を採掘し、スコアを獲得するゲームを機動するコマンド。
 * スコアは鉱石によって変わり、採掘した鉱石の合計によってスコアが変動します。
 * 結果はプレーヤー名、点数、日時などで保存されます。
 */
public class MiningGameCommand extends BaseCommand implements Listener {

  public static final int GAME_TIME = 20;
  private Main main;
  private List<PlayerScore> playerScoreList = new ArrayList<>();
  private List<BlockState> originBlockState = new ArrayList<BlockState>();
  private List<ItemStack[]> playerInventories = new ArrayList<>();


  public MiningGameCommand(Main main) {
    this.main = main;
  }


  @Override
  public boolean onExecutePlayerCommand(Player player) {
    PlayerScore nowPlayer = getPlayerScore(player);
    // プレーヤーのインベントリを保存
    playerInventories.add(player.getInventory().getContents());

    //プレーヤーの初期ステータス設定（体力・空腹値・装備）
    initPlayerStatus(player);

    gamePlay(player, nowPlayer);

    return true;
  }


  @Override
  public boolean onExecuteNPCCommand(CommandSender sender) {
    return false;
  }


  /**
   * 新規のプレーヤー情報をリストに追加する。
   *
   * @param player 　コマンドを実行したプレーヤー。
   * @return 新規プレーヤー
   */
  private PlayerScore addNwePlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore(player.getName());
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

    for (PlayerScore playerScore : playerScoreList) {
      if (playerScore.getPlayerName().equals(player.getName())) {
        int nowScore = playerScore.getScore();
        int scoreIncrement = 0;

        // 採掘されたブロックの種類を取得し各スコアを与える
        switch (e.getBlock().getType()) {
          case COAL_ORE -> scoreIncrement = 2;
          case IRON_ORE -> scoreIncrement = 5;
          case EMERALD_ORE -> scoreIncrement = 10;
          case LAPIS_ORE -> scoreIncrement = 15;
          case DIAMOND_ORE -> scoreIncrement = 20;
        }

        playerScore.setScore(nowScore + scoreIncrement);

        if (scoreIncrement > 0) {
          player.sendMessage( "鉱石を採掘！現在のスコアは" + playerScore.getScore() + "点");
        }
      }
    }
  }


  /**
   * 現在実行しているプレーヤーのスコア情報を取得する。
   * @param player 　コマンド実行プレーヤー
   * @return　現在実行しているプレーヤーのスコア情報
   */
  private PlayerScore getPlayerScore(Player player) {
    PlayerScore playerScore = new PlayerScore(player.getName());
    if (playerScoreList.isEmpty()) {
      playerScore = addNwePlayer(player);
    } else {
      playerScore = playerScoreList.stream().findFirst().map(ps
          -> ps.getPlayerName().equals(player.getName())
          ? ps
          : addNwePlayer(player)).orElse(playerScore);
    }
    playerScore.setGameTime(GAME_TIME);
    return playerScore;
  }


  /**
   * ゲーム開始時のプレーヤーの状態の設定 体力・空腹値を20(最大）にし、右手装備をダイヤモンドピッケルにする
   * @param player 　コマンド実行プレーヤー
   */
  private void initPlayerStatus(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);
    player.sendTitle("鉱石採掘ゲーム スタート!", "", 5, 40, 5);

    player.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_PICKAXE));

    Location playerLocation = player.getLocation();

    double x = playerLocation.getX() + 7;
    double y = playerLocation.getY() + 3; //sizeの半数値で地上に生成
    double z = playerLocation.getZ() ;

    // 新しい範囲の中心座標
    Location center = new Location(playerLocation.getWorld(), x, y, z);
    generateRandomBlocks(center, 7);

  }


  /**
   * @param center
   * @param size
   */
  private List<BlockState> generateRandomBlocks(Location center, int size) {
    World world = center.getWorld();

    for (int x = -size / 2; x <= size / 2; x++) {
      for (int y = -size / 2; y <= size / 2; y++) {
        for (int z = -size / 2; z <= size / 2; z++) {
          Location blockLocation = new Location(world,
              center.getX() + x, center.getY() + y, center.getZ() + z);
          Block block = world.getBlockAt(blockLocation);
          originBlockState.add(block.getState());

          // ランダムなブロックの種類を選択
          block.setType(getRandomBlockType());
        }
      }
    }
    return originBlockState;
  }


  /**
   * ゲームの実行。規定の時間内に獲得した鉱石によってスコアが加算されます。
   * 合計スコアを時間経過とともに表示します。
   * @param player　コマンド実行プレーヤー
   * @param nowPlayer　プレイヤースコア情報
   */
  private void gamePlay(Player player, PlayerScore nowPlayer) {
    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if (nowPlayer.getGameTime() <= 0) {
        Runnable.cancel();

        player.sendTitle("ゲーム終了！",
            nowPlayer.getPlayerName() + " 合計 " + nowPlayer.getScore() + "点！",
            5, 40, 5);

        // プレーヤーのインベントリを元に戻す
        player.getInventory().setContents(playerInventories.get(playerInventories.size() - 1));
        playerInventories.remove(playerInventories.size() - 1);

        nowPlayer.setScore(0);

        // ブロックの元の状態に戻す
        restoreOriginalBlocks();

        originBlockState.clear();
        return;
      }
      nowPlayer.setGameTime(nowPlayer.getGameTime() - 20);
    }, 0, 30 * 20);
  }


  private void restoreOriginalBlocks() {
    for (BlockState blockState : originBlockState) {
      blockState.update(true, false); // ブロックの元の状態に復元
    }
  }


  /**
   *
   * @return
   */
  private Material getRandomBlockType() {
    int random = new SplittableRandom().nextInt(100);

    if (random < 3) {
      return Material.DIAMOND_ORE;   // 3%
    } else if (random < 8) {
      return Material.LAPIS_ORE;     // 5%
    } else if (random < 18) {
      return Material.EMERALD_ORE;   // 10%
    } else if (random < 33) {
      return Material.IRON_ORE;      // 15%
    } else if (random < 48) {
      return Material.COAL_ORE;      // 15%
    } else {
      return Material.STONE;         // 52%
    }
  }
}



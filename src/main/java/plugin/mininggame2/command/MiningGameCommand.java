package plugin.mininggame2.command;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import plugin.mininggame2.Main;
import plugin.mininggame2.PlayerScoreData;
import plugin.mininggame2.data.ExecutingPlayer;
import plugin.mininggame2.mapper.data.PlayerScore;

/**
 * 制限時間内にランダムで出現する鉱石（ブロック）を採掘し、スコアを獲得するゲームを機動するコマンド。
 * スコアは鉱石によって変わり、採掘した鉱石の合計によってスコアが変動します。
 * 結果はプレーヤー名、点数、日時などで保存されます。
 */
public class MiningGameCommand extends BaseCommand implements Listener {

  public static final int GAME_TIME = 20;
  public static final String EASY = "easy";
  public static final String NORMAL = "normal";
  public static final String HARD = "hard";
  public static final String NONE = "none";
  public static final String List = "list";

  private final Main main;
  private final PlayerScoreData playerScoreData = new PlayerScoreData();

  List<ExecutingPlayer> executingPlayerList = new ArrayList<>();
  private final List<BlockState> originBlockState = new ArrayList<>();
  private final List<Block> generateBlocks = new ArrayList<>();
  private final List<ItemStack[]> playerInventories = new ArrayList<>();

  public MiningGameCommand(Main main){
    this.main = main;
  }


  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
    //最初の引数が『List』だった場合はスコア情報一覧を表示して、処理を終了する。
    if (args.length == 1 && (List.equals(args[0]))) {
      sendPlayerScorelist(player);
      return false;
    }

    String difficulty = getDifficulty(player, args);
    if (difficulty.equals(NONE)){
      return false;
    }

    ExecutingPlayer nowPlayer = getPlayerScore(player);

    // プレーヤーのインベントリを保存
    playerInventories.add(player.getInventory().getContents());

    //プレーヤーの初期ステータス設定（体力・空腹値・装備）
    initPlayerStatus(player, difficulty);

    gamePlay(player, nowPlayer, difficulty);

    return true;
  }


  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label, String[] args) {
    return false;
  }


  /**
   * 新規のプレーヤー情報をリストに追加する。
   *
   * @param player 　コマンドを実行したプレーヤー。
   * @return       　新規プレーヤー
   */
  private ExecutingPlayer addNwePlayer(Player player) {
    ExecutingPlayer newPlayer = new ExecutingPlayer(player.getName());
    executingPlayerList.add(newPlayer);
    return newPlayer;
  }


  /**
   * 現在登録されているスコア一覧をメッセージに送る。
   *
   * @param player　プレーヤー
   */
  private void sendPlayerScorelist(Player player) {
    java.util.List<PlayerScore> playerScoreList = playerScoreData.selectList();
    for (PlayerScore playerScore : playerScoreList){
      player.sendMessage(playerScore.getId() + " | "
          + playerScore.getPlayer_name() + " | "
          + playerScore.getScore() + " | "
          + playerScore.getDifficulty() + " | "
          + playerScore.getRegistered_at().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
  }


  /**
   * 難易度をコマンド引数から取得します。
   *
   * @param player　コマンド実行者
   * @param args　  コマンド引数
   * @return      　難易度
   */
  private String getDifficulty(Player player, String[] args) {
    if (args.length == 1 &&
        (EASY.equals(args[0]) || NORMAL.equals(args[0]) || HARD.equals(args[0]))) {
      return args[0];
    }
    player.sendMessage(ChatColor.RED
        + "ゲームの実行には難易度の指定が必要です。(例:/miningGame easy)");
    return NONE;
  }


  /**
   * ゲーム開始時のプレーヤーの状態の設定 体力・空腹値を20(最大）にし、右手装備をダイヤモンドピッケルにする。
   *
   * @param player     　コマンド実行プレーヤー
   * @param difficulty 　難易度
   */
  private void initPlayerStatus(Player player, String difficulty) {
    player.setHealth(20);
    player.setFoodLevel(20);

    player.sendTitle("鉱石採掘ゲーム スタート!", "", 5, 40, 5);

    player.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_PICKAXE));

    Location playerLocation = player.getLocation();

    double x = playerLocation.getX() + 7;
    double y = playerLocation.getY() + 3;
    double z = playerLocation.getZ() ;

    // 新しい範囲の中心座標
    Location center = new Location(playerLocation.getWorld(), x, y, z);
    generateRandomBlocks(center, difficulty);
  }


  /**
   * 現在実行しているプレーヤーのスコア情報を取得する。
   *
   * @param player 　コマンド実行プレーヤー
   * @return 　　　　 現在実行しているプレーヤーのスコア情報
   */
  private ExecutingPlayer getPlayerScore(Player player) {
    ExecutingPlayer executingPlayer = new ExecutingPlayer(player.getName());
    if (executingPlayerList.isEmpty()) {
      executingPlayer = addNwePlayer(player);
    } else {
      executingPlayer = executingPlayerList.stream().findFirst().map(ps
          -> ps.getPlayerName().equals(player.getName())
          ? ps
          : addNwePlayer(player)).orElse(executingPlayer);
    }
    executingPlayer.setGameTime(GAME_TIME);
    executingPlayer.setScore(0);
    removePotionEffect(player);
    return executingPlayer;
  }


  /**
   * ブロック採掘イベントを処理するクラスのメソッド。
   * 特定のブロックが採掘されたときに呼び出され、ブロックに応じてスコアを更新します。
   *
   * @param e　ブロック採掘イベント
   */
  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    // ブロックを採掘したプレーヤーを取得

    Player player = e.getPlayer();

    boolean isBlock = generateBlocks.stream()
        .anyMatch(blockState -> blockState.equals(e.getBlock()));

    if (!isBlock) {
      return;
    }

    executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst()
        .ifPresent(p -> {
          int nowScore = p.getScore();
          int scoreIncrement = 0;

          // 採掘されたブロックの種類を取得し各スコアを与える
          switch (e.getBlock().getType()) {
            case COAL_ORE -> scoreIncrement = 2;
            case IRON_ORE -> scoreIncrement = 5;
            case EMERALD_ORE -> scoreIncrement = 10;
            case LAPIS_ORE -> scoreIncrement = 15;
            case DIAMOND_ORE -> scoreIncrement = 20;
          }

          p.setScore(nowScore + scoreIncrement);

          String blockName = getBlockName(e.getBlock().getType());

          if (scoreIncrement > 0) {
            player.sendMessage( blockName +  "を採掘！現在のスコアは" + p.getScore() + "点");
          }
        });
  }


  // ブロックに対応した名前を取得
  private String getBlockName(Material material) {
    return switch (material) {
      case COAL_ORE -> "石炭";
      case IRON_ORE -> "鉄";
      case EMERALD_ORE -> "エメラルド";
      case LAPIS_ORE -> "ラピスラズリ";
      case DIAMOND_ORE -> "ダイヤモンド";
      default -> material.name();
    };
  }


  /**
   * ゲームの実行。規定の時間内に獲得した鉱石によってスコアが加算。 合計スコアを時間経過とともに表示する。
   *
   * @param player         　 　 コマンド実行プレーヤー
   * @param nowExecutingPlayer  プレイヤースコア情報
   * @param difficulty          難易度
   */
  public void gamePlay(Player player, ExecutingPlayer nowExecutingPlayer, String difficulty){
    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if (nowExecutingPlayer.getGameTime() <= 0) {
        Runnable.cancel();

        player.sendTitle("ゲーム終了！",
            nowExecutingPlayer.getPlayerName() + " 合計 " + nowExecutingPlayer.getScore() + "点！",
            5, 40, 5);

        // プレーヤーのインベントリを元に戻す
        player.getInventory().setContents(playerInventories.get(playerInventories.size() - 1));
        playerInventories.remove(playerInventories.size() - 1);

        removePotionEffect(player);

        // ブロックの元の状態に戻す
        restoreOriginalBlocks();
        originBlockState.clear();
        generateBlocks.clear();

        playerScoreData.insert(
            new PlayerScore(nowExecutingPlayer.getPlayerName()
            , nowExecutingPlayer.getScore()
            , difficulty));

        return;
      }
      nowExecutingPlayer.setGameTime(nowExecutingPlayer.getGameTime() - 20);
    }, 0, 30 * 20);

  }


  /**
   *　指定された中心座標を中心にランダムなブロックを生成するメソッド。
   *
   * @param center　　　　生成するブロックの中心座標
   * @param difficulty 　難易度
   */
  private void generateRandomBlocks(Location center, String difficulty) {
    World world = center.getWorld();

    for (int x = -7 / 2; x <= 7 / 2; x++) {
      for (int y = -7 / 2; y <= 7 / 2; y++) {
        for (int z = -7 / 2; z <= 7 / 2; z++) {
          Location blockLocation = new Location(world,
              center.getX() + x, center.getY() + y, center.getZ() + z);
          Block block = Objects.requireNonNull(world).getBlockAt(blockLocation);
          originBlockState.add(block.getState());

          // ランダムなブロックの種類を選択
          block.setType(getRandomBlockType(difficulty));
          generateBlocks.add(block);
        }
      }
    }
  }


  /**
   *指定された難易度に基づいてランダムなブロックの種類を取得するメソッド。
   *ランダムな値と難易度に応じて、異なる確率で各種類のブロックを取得します。
   *
   * @param  difficulty ゲーム難易度
   * @return            ブロック
   */
  private Material getRandomBlockType(String difficulty) {
    int random = new SplittableRandom().nextInt(100);
    switch (difficulty) {
      case EASY -> {
        if (random < 15) {
          return Material.DIAMOND_ORE;   // 15%
        } else if (random < 35) {
          return Material.EMERALD_ORE;   // 20%
        } else if (random < 65) {
          return Material.LAPIS_ORE;     // 30%
        } else {
          return Material.STONE;         // 45%
        }
      }
      case NORMAL -> {
        if (random < 3) {
          return Material.DIAMOND_ORE;   // 3%
        } else if (random < 13) {
          return Material.EMERALD_ORE;   // 10%
        } else if (random < 28) {
          return Material.IRON_ORE;      // 15%
        } else if (random < 43) {
          return Material.COAL_ORE;      // 15%
        } else {
          return Material.STONE;         // 57%
        }
      }
      case HARD -> {
        if (random < 3) {
          return Material.DIAMOND_ORE;   // 3%
        } else if (random < 13) {
          return Material.EMERALD_ORE;   // 10%
        } else if (random < 28) {
          return Material.LAPIS_ORE;     // 15%
        } else if (random < 43) {
          return Material.STONE;         // 15%
        } else {
          return Material.BEDROCK;       // 57%
        }
      }
    }
    return Material.STONE;
  }


  /**
   * プレーヤーに設定されている特殊効果を除外します。
   *
   * @param player　コマンド実行プレーヤー
   */
  private void removePotionEffect(Player player) {
    player.getActivePotionEffects().stream()
        .map(PotionEffect::getType)
        .forEach(player::removePotionEffect);
  }


  /**
   * generateRandomBlocksメソッドで生成されたブロックを生成前の状態に戻すメソッド。
   */
  private void restoreOriginalBlocks() {
    for (BlockState blockState : originBlockState) {
      blockState.update(true, false);
    }
  }

}



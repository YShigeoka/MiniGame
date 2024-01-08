package plugin.mininggame2.data;


import lombok.Getter;
import lombok.Setter;

/**
 * ゲーム実行する際のプレーヤー情報を扱うオブジェクト
 *
 */

@Getter
@Setter
public class ExecutingPlayer {

  private String playerName;
  private int score;
  private  int gameTime;


  public ExecutingPlayer(String playerName) {
    this.playerName = playerName;
  }
}

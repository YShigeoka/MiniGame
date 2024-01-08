package plugin.mininggame2.mapper.data;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * プレーヤーのスコア情報を扱うオブジェクト
 * DBに存在するテーブルと連動する。
 */

@Getter
@Setter
@NoArgsConstructor

public class PlayerScore {

  private int id;
  private String player_name;
  private int score;
  private String difficulty;
  private LocalDateTime registered_at;


  public PlayerScore(String player_name, int score, String difficulty){
    this.player_name = player_name;
    this.score = score;
    this.difficulty = difficulty;
  }
}

# Name
 
### 鉱石採掘ゲーム

* Minecraft内で特定の鉱石を採掘するとスコアが加算されるゲームをするためのプラグインです。
* Java初学者の学習の一環として作成しています。
* 2024.1.15 開発途中

> [!NOTE]
> 
> あくまでも初学者の開発ゲームです。温かい目でご利用いただけますと嬉しいです…！
> 
> 優しいご意見、ご指導を大歓迎しております！
> 

---

# DEMO




https://github.com/YShigeoka/MiniGame/assets/144694165/f7525147-b73f-44ab-99a2-87eb055b6b6b





---

# Requirement

* Java版 Minecraft version 1.20.1
* Spigot 1.20.1
* JDK 17
* MyBatis
* MySQL

---

# Usage

* 「/mininggame 難易度」コマンド入力すると、ゲームを開始します。
* ゲーム開始後、難易度に応じた鉱石を含むブロック群が出現。その鉱石を採掘したスコアを競います。
* 鉱石によって出現割合と獲得スコアが異なります。
  * 石炭：2点、　鉄：5点、　エメラルド：10点、　ラピスラズリ：15点、　ダイヤモンド：20点
* ゲーム時間は２０秒です。
* コマンド実行とともにプレーヤーの体力、空腹値がMaxとなり、ダイヤモンドのピッケルが装備されます。

---
# Command
* 「/mininggame 難易度」・・・ゲームが開始されます。難易度には [easy] [normal] [hard]の３種類があります。
* 「/mininggame list」・・・過去のゲーム結果を表示することができます。
* 「/game cancel」・・・鉱石採掘ゲームを強制終了します。
---

#  Note
* ひらけた場所でのコマンド入力をお勧めします。
* 鉱石の採掘時点でスコアが加算されます。鉱石の取得は関係していません。
* Macのみ動作確認済みです。
* マルチプレイには対応しておりません。
> [!CAUTION]
> 「game cancel」コマンドを実行した場合は、メインハンドに装備していたものがダイヤモンドピッケルに上書きされますのでご注意ください。
> また、出現したブロックについては消滅しませんので、必要であればfillコマンド等を使用し整地してください。

---
#  Selling Points!
* プレーヤーの周辺にブロックが出現するようになっています。好きな所でゲームができます！
* 難易度に応じて出現する鉱石の種類や出現割合は変化します。採掘自体が困難な鉱石も出現します！
* ゲーム中に装備したピッケルや出現ブロックはゲーム終了とともに、元の状態に戻ります。


---

# Author
 
* 作成者　Y.Shigeoka
* X(旧Twitter)　https://twitter.com/ura220002

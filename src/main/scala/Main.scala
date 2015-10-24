import slick.driver.MySQLDriver.api._
import com.typesafe.config._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
//import scala.util.{Success, Failure}
import scala.concurrent.duration.Duration
import slick.dbio.DBIO

object main {
    def main(args:Array[String]):Unit = {
    println("START")
    val config = ConfigFactory.load()
    
    if (args.length > 0) {
      //debug;非同期実行では結果が取得できない場合があるので同期(Await)待ち
//      val dbr1 = WpOptions.findById(1)
//      println(Await.result(dbr1, Duration.Inf).option_value)
      
      args(0) match {
        case "global" => 
          val up = for {
            r1 <- WpOptions.findById(1)
            r2 <- WpOptions.update(r1.option_id.get, r1.copy(option_value = config.getString("app.globalUrl")))
            r3 <- WpOptions.findById(2)
            r4 <- WpOptions.update(r3.option_id.get, r3.copy(option_value = config.getString("app.globalUrl")))
          } yield r2
          
          //上記のようなFor文（flatMap）でないと同期更新が正しくできない模様（以下の記述はAwait入れても同期されない（更新されない））
          //r1.map(row => Await.result(WpOptions.update(1, row.copy(option_value = config.getString("app.globalUrl"))),Duration.Inf))
          //val up = r1.map(row => WpOptions.update(row.option_id.get, WpOptionsRow(row.option_id, row.option_name, config.getString("app.globalUrl"), row.autoload)))
          
          //同期しないと更新されない（メインスレッドが先に終わると処理中断される？）
          Await.result(up, Duration.Inf)
          println("global update")

        case "private" =>
          val up = for {
            r1 <- WpOptions.findById(1)
            r2 <- WpOptions.update(r1.option_id.get, r1.copy(option_value = config.getString("app.privateUrl")))
            r3 <- WpOptions.findById(2)
            r4 <- WpOptions.update(r3.option_id.get, r3.copy(option_value = config.getString("app.privateUrl")))
          } yield r2
          Await.result(up, Duration.Inf)
          println("private update")
        case _ => println("「global」 or 「private」 を指定して下さい。")
      }

      //更新後再検索
//      val dbr2 = WpOptions.findById(1)
//      println(Await.result(dbr2, Duration.Inf).option_value)
    } else {
      println("パラメータを指定してください")
    }

    println("END")
  }
}

//wp_optionsのレコードケースクラス
case class WpOptionsRow(option_id: Option[Int], option_name: String, option_value: String, autoload: String)

//wp_optionsのテーブルクラス
class WpOptions(tag: Tag) 
      extends Table[WpOptionsRow](tag, "wp_options") {
  def option_id = column[Int]("OPTION_ID", O.PrimaryKey)
  def option_name = column[String]("OPTION_NAME")
  def option_value = column[String]("OPTION_VALUE")
  def autoload = column[String]("AUTOLOAD")
  def * = (option_id.?, option_name, option_value, autoload) <> (WpOptionsRow.tupled, WpOptionsRow.unapply)
}

//wp_options操作オブジェクト
object WpOptions {
  val wpoptions = TableQuery[WpOptions]
  
  //DB設定読み込み（com.typesafe.config）
  def db: Database = Database.forConfig("DbSetting")

  //select(キー検索)文
  def filterQuery(id: Int): Query[WpOptions, WpOptionsRow, Seq] =
    wpoptions.filter(_.option_id === id)
    
  //select(キー検索)実行
  def findById(id: Int): Future[WpOptionsRow] =
      try db.run(filterQuery(id).result.head)
      finally db.close

  //insert実行
  def insert(row: WpOptionsRow): Future[Int] = 
    try db.run(wpoptions += row)
    finally db.close
 
  //update実行
  def update(id: Int, row: WpOptionsRow): Future[Int] =
    try db.run(filterQuery(id).update(row))
    finally db.close
 
  //delete実行
  def delete(id: Int): Future[Int] =
    try db.run(filterQuery(id).delete)
    finally db.close
}

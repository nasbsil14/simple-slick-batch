lazy val root = (project in file(".")).
  settings(
    name := "wordpress_mente",
    version := "1.0",
    scalaVersion := "2.11.6"
  )

//参照リポジトリ追加
//resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

//依存ライブラリ設定
// 「"グループID" % "アーティファクトID" % "バージョン"」で記述（グループID後を「%%」とするとscalaのバージョンに合ったjarをダウンロードする）
libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.0.0",
    "com.typesafe" % "config" % "1.3.0",
    "mysql" % "mysql-connector-java" % "5.1.35",
    "org.slf4j" % "slf4j-nop" % "1.6.4"
)

//jarのローカルコピー有無
//retrieveManaged := true
nor (version 0.3)
=======================================

概要
---------------------------------------
nor はプラグインにより機能を拡張することができる，ローカルプロキシプラットフォームです．


インストール
---------------------------------------
適当なフォルダにアーカイブを展開してください．
lib フォルダには追加で必要となる外部ライブラリを，plugin フォルダにはプラグインを格納します．
また，実行には Java Runtime Environment (JRE) 6 以降が必要になります．
別途，http://www.java.com/ などから取得してインストールしてください．


アンインストール
---------------------------------------
フォルダを削除してください．


使い方
---------------------------------------
Windowsでは bin/nor.bat を，Linux では bin/nor を用いて起動できます．
デフォルトでは，127.0.0.1:8080 で待ち受けます．Proxy auto-config ファイルを提供するため，
お使いのブラウザの設定で，

    http://127.0.0.1:8080/nor/core/proxy.pac

を設定してください．（127.0.0.1:8080の部分は設定内容に合わせてください）
終了方法は，プロンプトに何かキーを入力するかウインドウを閉じて下さい．

config フォルダには設定ファイルが格納されます．
nor では，コンピュータが利用されている環境毎に異なる設定を用いることが可能です．
例えば，ご家庭の信頼できるネットワーク用の設定とフリーホットスポット用の設定を分けることができます．
そのため，nor では，二種類の設定ファイルを用います．
まず，config/nor.conf は，すべてのネットワーク環境に共通な設定を記述するファイルです．
次に，config/<host>/nor.conf は，ネットワーク環境別の設定を記述するファイルです．
<host>の部分は，接続中ネットワークのゲートウェイホスト名（設定されてない場合は IP アドレス）になります．
該当するフォルダが無い場合は，一度 nor を起動すれば作成されます．

nor 以外に別のプロキシを使用する必要がある場合，config/<host>/routing.conf で設定することができます．
記述方法は，次のフォーマットに従い，一行に一つの設定を記述します．

    <別のプロキシを利用するURL正規表現>=<適用するプロキシのホスト名>:<ポート番号>

正規表現中にコロン(:)を使用する場合は，\\ でエスケープして下さい．
例えば，
    http\\://.*\.example.com/=http://another.proxy.net:8080
という設定は，http://www.example.com/ や http://dev.example.com/ などへの要求に対して，
http://another.proxy.net:8080 をプロキシとして使用することを表します．


ライセンス
---------------------------------------
本ソフトウェアは、GNU 一般公衆利用許諾書(GNU GPL)バージョン 3 のもとで配布されています。

このプログラムはフリーソフトウェアです．
あなたはこれを，フリーソフトウェア財団によって発行されたGNU 一般公衆利用許諾書(バージョン3か、それ以降のバージョンのうちどれか)
が定める条件の下で再頒布または改変することができます．

このプログラムは有用であることを願って頒布されますが，全くの無保証です．
商業可能性の保証や特定目的への適合性は，言外に示されたものも含め，全く存在しません．
詳しくはGNU 一般公衆利用許諾書をご覧ください．


ライブラリについて
---------------------------------------
本ソフトウェアは，Apache Software Foundation による下記のライブラリを使用しています．
This product includes software developed by
The Apache Software Foundation (http://www.apache.org/).

- Apache Velocity
Copyright (C) 2000-2007 The Apache Software Foundation

- Apache Commons CLI
Copyright 2001-2009 The Apache Software Foundation

- Apache Commons Collections
Copyright 2001-2008 The Apache Software Foundation

- Apache Commons Lang
Copyright 2001-2011 The Apache Software Foundation

- Apache Commons Codec
Copyright 2002-2009 The Apache Software Foundation
(src/test/org/apache/commons/codec/language/DoubleMetaphoneTest.java contains
test data from http://aspell.sourceforge.net/test/batch0.tab.
Copyright (C) 2002 Kevin Atkinson (kevina@gnu.org). Verbatim copying
and distribution of this entire article is permitted in any medium,
provided this notice is preserved.)


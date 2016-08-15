# 概要

nor はプラグインによって機能を拡張することのできる統合型プロキシシステムです．

現在，セキュリティ向上やキャッシュによるアクセス速度の向上，あるいは Web 開発など様々な用途にローカルプロキシサーバが使用されています． これらのプロキシサーバでは，一つのサーバが一つの機能を提供していることが大半です． そのため，複数の機能が必要な場合，データをそれぞれのプロキシサーバに順に通す，いわゆる多段プロキシが必要になります．

プロキシサーバには，ポートの監視，HTTPリクエストやレスポンスの解析などといった操作が必要ですが， 複数のプロキシサーバを繋げて使用する場合，個々のサーバがそれぞれ上記の操作を行うことになりオーバーヘッドが大きくなります．

本ソフトウェアを用いることで，この共通操作をまとめることができます．

## 利用方法

- [インストール](http://sourceforge.jp/projects/nor/howto/install)
- [設定及び起動方法](http://sourceforge.jp/projects/nor/howto/usage)

## プラグイン一覧

- [HttpsConnectorPlugin](https://osdn.jp/projects/nor/wiki/HttpsConnectorPlugin)
- [ManagementArticlesPlugin](https://osdn.jp/projects/nor/wiki/ManagementArticlesPlugin)
- [NicoCacheNorPlugin](https://osdn.jp/projects/nor/wiki/NicoCacheNorPlugin)

## 開発者向け情報

- [APIリファレンス](http://nor.sourceforge.jp/api/)


# License
This software is released under The GNU General Public License Version 3, see [COPYING](COPYING) for more detail.

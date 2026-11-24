# CustomJoinMessages 啾啾欢迎奖励

版本：17.12.0-jiujiu-svfix3；目标API Paper26.2，JDK25，Gradle9.8.2。

本工程以原作者v17.12.0完整源码为主体，保留旧正式服svfix1的SuperVanishVanishHook.java适配源码（逐字节复用），加入首登点击欢迎奖励100 J点及管理员测试功能。不是旧补丁工程原封不动重编；不使用JarRepair、ASM、字节码补丁或反编译代码。调度、奖励及构建适配按当前Folia与共享货币接口维护。保留原作者署名和GPL许可证。

插件注册名及配置目录保持CustomJoinMessages，成品CustomJoinMessages.jar。旧命令/cjm、/customjoinmessages保留，/jiujiuhuanying仍作为别名。j货币通过JiuJiuLib，默认首次进服60秒内点击、每人每位新人一次、每日不限次数、不得自领。data/welcome-rewards.journal记录付款意图，未知结果不补发；原世界/玩家数据不清理。

/cjm testwelcome 在线玩家 preview：仅管理员预览，不发钱；paid模式真实发奖，不限每日次数，需另一名管理员点击。测试资格独立，不改真实首登记录。

配置以用户132321.zip为结构和开关基线，将玩家消息翻译中文；data/toggles.json及worlds.json不打入覆盖包，保留服务器现有数据。language:en管理命令语言保留。welcome-rewards.yml未知服务器自定义不覆盖，缺少时从JAR生成，已有时缺少消息使用默认值。

编译：gradlew.bat build -Pversion.metadata=false，首次需联网下载依赖。dependencies/JiuJiuLib.jar和SuperVanish-api-reference.jar是仅供编译的真实接口参考，不是本插件成品，不打入成品，也不要安装替换服务器依赖。隐身适配仅针对用户dev.philippcmd SuperVanish系列，不改隐身插件。

本地构建及测试不等于实服兼容验收。原PlayerTeleportEvent世界公告在folia-plugin-dev中被列为不可用，本配置保持关闭。日志实际Shiroha26.2/f44c27e和SuperVanish stealth4需实际验收。

---
[![Spigot Downloads][spigot-downloads-shield]][spigot-url]
[![bStats Servers][bstats-servers-shield]][bstats-url]
[![Spigot Rating][spigot-rating-shield]][spigot-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![GNU License][license-shield]][license-url]
[![Discord][discord-shield]][discord-url]




<!-- PROJECT LOGO -->
<div align="center">
  <h1>Custom Join Messages</h1>
  <p>
    The most feature-packed and highly customizable plugin for join/quit notifications on the market
    <br />
    <br />
    <a href="https://github.com/Insprill/Custom-Join-Messages/issues">Report Bug</a>
    ·
    <a href="https://github.com/Insprill/Custom-Join-Messages/issues">Request Feature</a>
  </p>
</div>




<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#features">Features</a></li>
    <li>
      <a href="#download">Download</a>
      <ul>
        <li><a href="#releases">Releases</a></li>
        <li><a href="#snapshots">Snapshots</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#building">Building</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Support</a></li>
  </ol>
</details>




<!-- FEATURES -->
## Features

* 📢 Multiple Message Types
    * Chat
    * Titles
    * Actionbar
    * Bossbar
    * Sounds

* 💬 Advanced Formatting
    * All messages support [PlaceholderAPI][placeholderapi-url] placeholders, including nested placeholders (up to 10 levels)
    * All messages support different formatters to fit your needs
        * [Legacy Color Codes][legacy-color-codes-url]
        * [MiniMessage][minimessage-url] ([Paper][papermc-url] only)
        * [MineDown][minedown-url]
    * All messages support HEX colors
    * All messages support automatic gradients when using MineDown or MiniMessage
    * Chat messages support hover/click actions when using MineDown or MiniMessage

* 🌎 World-Based Messages
    * Treat a single server with multiple worlds as separate servers
    * Customizable groups for what worlds count as the same "server"

* 🗝️ Permission-Based Messages
    * Send different messages based on what permissions a user has

* 🎛️ Message Conditions
    * Radius
    * Min/Max Online Players

* 🔒 AuthMe Integration
    * Only send messages once players have authenticated themselves

* ⛓️ Jail Integration
    * Don't send messages for jailed players
    * Supports most jail plugins

* 🥷 Vanish Integration
    * Send messages when vanishing/unvanishing
    * Supports most vanish plugins

* 🎚️ Fine-grained Message Toggling Per-Player
    * Allow your players to personalize which messages they receive

* 📜 Supports All Platforms
    * [Spigot][spigotmc-url]
    * [Paper][papermc-url]
    * [Folia][folia-url]
    * [Purpur][purpurmc-url]
    * Most other forks




<!-- DOWNLOAD -->
## Download
### Releases
Releases of CJM can be downloaded from [Modrinth][modrinth-url] (recommended), [CurseForge][curseforge-url], [Hangar][hangar-url], or [SpigotMC][spigot-url].

### Snapshots
Like living on the edge?
Builds from the latest commit can be downloaded via the [Snapshot Hangar channel][hangar-versions-url] or from [GitHub Actions][github-actions-url].




<!-- USAGE -->
## Usage

Unsure how something works? All documentation can be found on the [wiki][wiki-url].




<!-- BUILDING -->
## Building

To compile Custom Join Messages, you will need an internet connection and a terminal.
Clone this repo, then run `./gradlew build`.
You can find the compiled jar in the `build/libs` directory.




<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create.
Any contributions you make are **greatly appreciated**!
If you're new to contributing to open-source projects, you can follow [this](https://docs.github.com/en/get-started/quickstart/contributing-to-projects) guide.




<!-- Statistics -->
## Statistics

[![Statistics][bstats-servers-svg]][bstats-url]

Statistics are collected through [bStats][bstats-url],
an open-source service that collects anonymous data for Minecraft software. You can opt out in `plugins/bStats/config.yml`.



<!-- LICENSE -->
## License

Distributed under the GNU General Public License v3.0.
See [LICENSE][license-url] for more information.




<!-- SUPPORT -->
## Support

Found a bug? Want to request a feature? Head over to [the issue tracker][issues-url].\
Unsure how something works? Check out the [wiki][wiki-url].\
Still need personalized support? Subscribe to my [Patreon][patreon-url] and join my [Discord Server][discord-url].\
<sup>Once you've subscribed and joined, you can ask questions in the `#support` channel.</sup>


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[bstats-servers-svg]: https://bstats.org/signatures/bukkit/Custom%20Join%20Messages.svg
[bstats-servers-shield]: https://img.shields.io/bstats/servers/6346.svg?style=for-the-badge
[bstats-url]: https://bstats.org/plugin/bukkit/Custom%20Join%20Messages/6346
[spigot-rating-shield]: https://img.shields.io/spiget/rating/71608.svg?style=for-the-badge
[github-actions-url]: https://nightly.link/Insprill/custom-join-messages/workflows/gradle/develop
[curseforge-url]: https://www.curseforge.com/minecraft/bukkit-plugins/custom-join-messages
[hangar-url]: https://hangar.papermc.io/Insprill/Custom-Join-Messages
[hangar-versions-url]: https://hangar.papermc.io/Insprill/Custom-Join-Messages/versions
[modrinth-url]: https://modrinth.com/plugin/custom-join-messages
[spigot-url]: https://www.spigotmc.org/resources/71608
[stars-shield]: https://img.shields.io/github/stars/Insprill/Custom-Join-Messages.svg?style=for-the-badge
[stars-url]: https://github.com/Insprill/Custom-Join-Messages/stargazers
[issues-shield]: https://img.shields.io/github/issues/Insprill/Custom-Join-Messages.svg?style=for-the-badge
[issues-url]: https://github.com/Insprill/Custom-Join-Messages/issues
[license-shield]: https://img.shields.io/github/license/Insprill/Custom-Join-Messages.svg?style=for-the-badge
[license-url]: https://github.com/Insprill/Custom-Join-Messages/blob/master/LICENSE
[discord-shield]: https://img.shields.io/discord/626995215558901771?color=%235663F7&label=Discord&style=for-the-badge
[discord-url]: https://discord.gg/vjQ3F2XDTc
[spigot-downloads-shield]: https://img.shields.io/spiget/downloads/71608?style=for-the-badge
[patreon-url]: https://www.patreon.com/insprill
[wiki-url]: https://cjm.insprill.net/
[spigotmc-url]: https://www.spigotmc.org/
[papermc-url]: https://papermc.io/
[folia-url]: https://papermc.io/software/folia
[purpurmc-url]: https://purpurmc.org/
[placeholderapi-url]: https://hangar.papermc.io/HelpChat/PlaceholderAPI
[minedown-url]: https://github.com/Phoenix616/MineDown#syntax
[minimessage-url]: https://docs.advntr.dev/minimessage/index.html
[legacy-color-codes-url]: https://minecraft.wiki/w/Formatting_codes#Color_codes

管理员测试：`/cjm testwelcome <在线玩家> [preview|paid]`，默认preview不发钱；paid真实发奖，不限每日次数。测试目标必须在线、已登录且未隐身。测试按钮只向具有 `jiujiuhuanying.test` 权限的其他管理员显示，目标不能自领，每轮一次；同一时间只开放一轮测试，60秒后可再次开。每轮使用独立随机记录，不重置真实进服/领奖记录。旧奖励配置保留，新提示缺项从内置默认值补齐，不覆盖原文件。MCSM控制台命令不加前导斜线。


2026-11-24 隐身兼容恢复：识别SuperVanish的实际main为dev.philippcmd.supervanish.Main时，使用其公开getVanishManager().isVanished及VanishStateChangeEvent，不调用de.myzelyam接口。官方SuperVanish仍沿用官方挂钩。dependencies/SuperVanish-api-reference.jar仅作编译参考，不打入成品，不要求替换服务器隐身插件。服务器日志所示stealth4尚需实际验收，编译接口核对使用本地stealth3及D:/Github内同系列源码。

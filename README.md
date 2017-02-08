# ApkMultiChannel Plugin

这是一个为了方便 Android 多渠道打包的 Android Studio / IDEA 插件：
- 支持v2签名：jarsigner 和 apksigner
- 支持2种渠道打包方式：
    - 修改 AndroidManifest.xml 的 meta-data (name 为 UMENG_CHANNEL) 的 value 值，没有则插入，然后重新打包签名
    - 美团的方式，直接往 apk 的 META-INF 目录里添加空文件（文件名为：c_渠道）
    - packer-ng-plugin的方式，往 apk 注释字段保存渠道号和验证标记字节

## 安装

- 打开 Android Studio: 打开 ```Setting/Preferences -> Plugins -> Browse repositories```
然后搜索 ```ApkMultiChannel``` 安装重启

或者

- 下载 [ApkMultiChannelPlugin.jar](https://plugins.jetbrains.com/idea/plugin/9369)
然后 ```Setting/Preferences -> Plugins -> Install plugin from disk``` 选择 ```ApkMultiChannelPlugin.jar``` 安装重启


## 使用方式

#### 1. 选择 apk

选择一个 apk 然后右键，点击 Build MultiChannel

<img src="https://raw.githubusercontent.com/nukc/ApkMultiChannelPlugin/master/art/choose-apk.png">

#### 2. 配置

配置签名信息，打包方式和渠道等

<img src="https://raw.githubusercontent.com/nukc/ApkMultiChannelPlugin/master/art/setting.png">

配置说明：

**Key Store Path:** 签名文件的路径 <br/>
**Key Store Password:** 签名文件的密码 <br/>
**Key Alias:** 密钥别名 <br/>
**Key Password:** 密钥密码 <br/>

**Zipalign Path:** zipalign 文件的路径（用于优化 apk；zipalign 可以确保所有未压缩的数据均是以相对于文件开始部分的特定字节对齐开始，这样可减少应用消耗的 RAM 量。）<br/>
**Signer Version:** 选择签名版本：apksigner 和 jarsigner <br/>
**Build Type:** 打包方式 <br/>

**Channels:** 渠道列表，每行一个，最前面可加 ```>``` 或不加（保存信息的时候，程序会自行加上）

#### 3. 开始打包

配置完成之后按 OK 就会开始进行渠道打包，文件会输出在选中的apk的当前目录下的channels目录中

<img src="https://raw.githubusercontent.com/nukc/ApkMultiChannelPlugin/master/art/output.png">


## 说明

在开始打包前，配置信息先会保存在项目根目录的 channels.properties：

<img src="https://raw.githubusercontent.com/nukc/ApkMultiChannelPlugin/master/art/properties.png">

渠道可以直接在 channels.properties 添加（这里必须要在前面加 ```>```，为了方便解析，只有加了 ```>``` 才会参与打包），也可以删除和注释（#）

配置完后，想改配置但是又不想直接修改 channels.properties 文件的话，可以在 ```Build > Channel Setting``` 打开配置界面，
在这里打开配置界面按 OK 按钮后不会自动开始打包，仅用于修改配置。

### Build Type 说明

#### update AndroidManifest.xml
先解压提取 AndroidManifest.xml 文件到 temp 文件夹中，然后根据渠道列表配置修改 meta-data，复制1个 apk 到 channels 文件夹下（除了 META-INF 目录下的签名文件），
同时替换 AndroidManifest.xml，最后重新签名。

#### add channel file to META-INF
复制1个 apk，先检查签名版本，如果未签名则进行签名（配置选择 jarsigner 则在渠道打包前签名，apksigner 则是添加空文件到其 META-INF 目录后再签名）。
读取渠道：[ChannelHelper](https://gist.github.com/nukc/f777b54232be56f04171bcef56a627e1)

#### write zip comment
先判断选中的 apk 中 comment 是否含有 SIGN 字节，如果有则不进行渠道打包并提示；之后检查是否是 v2 签名，如果是 v2，则复制1个不带签名文件的 apk 到 temp 文件夹并重新签名为 v1，
最后开始渠道打包。（ v2 签名之后不能修改 ```ZIP End of Central Directory``` 区块，而且如果先修改 ```ZIP End of Central Directory``` 区块再 v2 签名，comment 会被抹掉。）
读取渠道：[ZipCommentHelper](https://gist.github.com/nukc/f762f73276c4ad02618147acd6978d16)

## 以后要加的功能

- 添加支持选择项目路径外的 apk 文件进行多渠道打包
- buildType 添加支持美团新一代渠道包生成方式 Walle

有什么问题欢迎大家在 [Issues](https://github.com/nukc/ApkMultiChannelPlugin/issues) 中提问

## 参考致谢

- [ntop001/AXMLEditor](https://github.com/ntop001/AXMLEditor)
- [Bilibili/apk-channelization](https://github.com/Bilibili/apk-channelization)
- [美团Android自动化之旅—生成渠道包](http://tech.meituan.com/mt-apk-packaging.html)
- [apksigner](https://developer.android.com/studio/command-line/apksigner.html)
- [packer-ng-plugin](https://github.com/mcxiaoke/packer-ng-plugin)
- [新一代开源Android渠道包生成工具Walle](http://tech.meituan.com/android-apk-v2-signature-scheme.html)
- [apksig](https://android.googlesource.com/platform/tools/apksig/)

同时感谢 [dim](https://github.com/zzz40500) 和 [区长](https://github.com/lizhangqu) 的指点迷津。

## License

GPL-3.0
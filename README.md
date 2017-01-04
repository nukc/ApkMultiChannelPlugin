# ApkMultiChannel Plugin

这是一个为了方便 Android 多渠道打包的 Android Studio / IDEA 插件：
- 支持v2签名：jarsigner 和 apksigner
- 支持2种渠道打包方式：
    - 修改 AndroidManifest.xml 的 meta-data (name 为 UMENG_CHANNEL) 的 value 值，没有则插入，然后重新打包签名
    - 美团的方式，直接往 apk 的 META-INF 目录里添加空文件（文件名为：c_渠道）

## 安装

- 打开 Android Studio: 打开 ```Setting/Preferences -> Plugins -> Browse repositories```
然后搜索 ```ApkMultiChannel``` 安装重启

或者

- 下载 [ApkMultiChannelPlugin.jar](https://plugins.jetbrains.com/idea/plugin/9369)
然后 ```Setting/Preferences -> Plugins -> Install plugin from disk``` 选择 ```ApkMultiChannelPlugin.jar``` 安装重启


## 使用方式

1. 选择一个 apk 然后右键，点击 Build MultiChannel

<img src="https://raw.githubusercontent.com/nukc/ApkMultiChannelPlugin/master/art/choose-apk.png">

2. 配置签名信息，打包方式和渠道等

<img src="https://raw.githubusercontent.com/nukc/ApkMultiChannelPlugin/master/art/setting.png">

配置说明：
Key Store Path: 签名文件的路径
Key Store Password: 签名文件的密码
Key Alias: 密钥别名
Key Password: 密钥密码

Zipalign Path: zipalign文件的路径（用于优化 apk；zipalign 可以确保所有未压缩的数据均是以相对于文件开始部分的特定字节对齐开始，这样可减少应用消耗的 RAM 量。）
Signer Version: 选择签名版本：apksigner 和 jarsigner
Build Type: 打包方式

Channels: 渠道列表，每行一个，最前面可加 ```>``` 或不加（保存信息的时候，程序会自行加上）

3. 配置完成之后按 OK 就会开始进行渠道打包，文件会输出在选中的apk的当前目录下的channels目录中

<img src="https://raw.githubusercontent.com/nukc/ApkMultiChannelPlugin/master/art/output.png">

在开始打包前，配置信息先会保存在项目根目录的 channels.properties：

<img src="https://raw.githubusercontent.com/nukc/ApkMultiChannelPlugin/master/art/properties.png">

渠道可以直接在 channels.properties 添加（这里必须要在前面加 ```>```，为了方便解析，只有加了 ```>``` 才会参与打包），也可以删除和注释（#）

配置完后，想改配置但是又不想直接修改 channels.properties 文件的话，可以在 ```Build > Channel Setting``` 打开配置界面，
在这里打开配置界面按 OK 按钮后不会自动开始打包，仅用于修改配置。

## 以后要加的功能

- 如果 buildType 选择美团方案在 META-INF 目录写入空文件:
    - 自定义空文件名的前辍（目前是 ```c_```）
    - 在打包之前先判断选中的apk是否已经签名，如果没有则先签名
  

## 参考致谢

- [ntop001/AXMLEditor](https://github.com/ntop001/AXMLEditor)
- [Bilibili/apk-channelization](https://github.com/Bilibili/apk-channelization)
- [美团Android自动化之旅—生成渠道包](http://tech.meituan.com/mt-apk-packaging.html)
- [apksigner](https://developer.android.com/studio/command-line/apksigner.html)

同时感谢 [dim](https://github.com/zzz40500) 和 [区长](https://github.com/lizhangqu) 的指点迷津。

## License

GPL-3.0
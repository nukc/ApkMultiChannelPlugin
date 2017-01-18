package com.github.nukc.plugin.helper;

import com.github.nukc.plugin.model.Options;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Nukc.
 */
public class CommandHelper {
    private static final Logger log = Logger.getInstance(CommandHelper.class);

    public static String exec(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream errorStream = process.getErrorStream();
            byte[] buffer = new byte[1024];
            int readBytes;
            StringBuilder stringBuilder = new StringBuilder();
            while ((readBytes = errorStream.read(buffer)) > 0) {
                stringBuilder.append(new String(buffer, 0, readBytes));
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void execSigner(Options options, String apkPath) {
        StringBuilder sb = new StringBuilder(options.signer);
        if ("jarsigner".equals(options.signer)) {
            sb.append(" -sigalg SHA1withRSA -digestalg SHA1 -keystore ");
            sb.append(options.keyStorePath);
            sb.append(" -storepass ").append(options.keyStorePassword);
            sb.append(" -keypass ").append(options.keyPassword);
            sb.append(" ").append(apkPath);
            sb.append(" ").append(options.keyAlias);
        } else {
            sb.append(" sign --ks ");
            sb.append(options.keyStorePath);
            sb.append(" --ks-pass ").append("pass:").append(options.keyStorePassword);
            sb.append(" --ks-key-alias ").append(options.keyAlias);
            sb.append(" --key-pass ").append("pass:").append(options.keyPassword);
            sb.append(" ").append(apkPath);
        }

        String cmd = sb.toString();
        log.info("signer : " + cmd);

        String result = CommandHelper.exec(cmd);
        if (!StringUtil.isEmpty(result)) {
            log.info(result);
        }
    }

    public static void execZipalign(Options options, String tempApkPath, String apkPath) {
        String cmd = options.zipalignPath + " -f 4 " + tempApkPath + " " + apkPath;
        log.info("zipalign : " + cmd);

        String result = CommandHelper.exec(cmd);
        if (!StringUtil.isEmpty(result)) {
            log.info(result);
        }
    }
}

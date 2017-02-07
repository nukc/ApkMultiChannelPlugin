package com.github.nukc.plugin.helper;

import com.github.nukc.plugin.model.Options;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nukc.
 */
public class OptionsHelper {

    private static final Logger log = Logger.getInstance(OptionsHelper.class);
    private static final String CHANNELS_PROPERTIES = "channels.properties";
    private static final String KEY_STORE_PATH = "key.store.path";
    private static final String KEY_STORE_PASSWORD = "key.store.password";
    private static final String KEY_ALIAS = "key.alias";
    private static final String KEY_PASSWORD = "key.password";
    private static final String ZIPALIGN_PATH = "zipalign.path";
    private static final String BUILD_TYPE = "build.type";
    private static final String SIGNER_PATH = "signer.path";

    public static final String BUILD_TYPE_UPDATE = "update AndroidManifest.xml";
    public static final String BUILD_TYPE_ADD = "add channel file to META-INF";
    public static final String BUILD_TYPE_ZIP_COMMENT = "write zip comment";

    private static String getPathName(Project project) {
        return project.getBasePath() + File.separator + CHANNELS_PROPERTIES;
    }

    public static Options load(Project project) {
        String pathname = getPathName(project);
        File file = new File(pathname);
        if (!file.exists()) {
            log.warn(pathname + " is not exists");
            return null;
        }

        Options options = new Options();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() != 0 && line.indexOf("#") != 0) {
                    if (line.indexOf(">") == 0) {
                        String channel = line.substring(1).trim();
                        if (channel.length() > 0) {
                            log.info("channel = " + channel);
                            options.channels.add(channel);
                        }
                    } else {
                        String[] properties = line.split("=");
                        if (properties.length < 2) {
                            log.info(line);
                            if (properties.length == 1) {
                                String key = properties[0];
                                log.warn(key + " is null");
                                continue;
                            }
                        }
                        String key = properties[0];
                        String value = properties[1];
                        log.info(key + " = " + value);

                        switch (key) {
                            case KEY_STORE_PATH:
                                options.keyStorePath = value;
                                break;
                            case KEY_STORE_PASSWORD:
                                options.keyStorePassword = value;
                                break;
                            case KEY_PASSWORD:
                                options.keyPassword = value;
                                break;
                            case KEY_ALIAS:
                                options.keyAlias = value;
                                break;
                            case ZIPALIGN_PATH:
                                options.zipalignPath = value;
                                break;
                            case BUILD_TYPE:
                                options.buildType = value;
                                break;
                            case SIGNER_PATH:
                                options.signer = value;
                                break;
                            default:
                                log.warn(key + " is not used; " + key + "=" + value);
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return options;
    }

    public static void save(Project project, String storeFilePath, String storePassword,
                            String keyPassword, String keyAlias, String zipalignPath, String[] channels,
                            String buildType, String signer) {
        List<String> lines = new ArrayList<>();
        lines.add("###### Sign Config ######");
        lines.add(KEY_STORE_PATH + "=" + storeFilePath);
        lines.add(KEY_STORE_PASSWORD + "=" + storePassword);
        lines.add(KEY_PASSWORD + "=" + keyPassword);
        lines.add(KEY_ALIAS + "=" + keyAlias);
        lines.add(ZIPALIGN_PATH + "=" + zipalignPath);
        lines.add(BUILD_TYPE + "=" + buildType);
        lines.add(SIGNER_PATH + "=" + signer);
        lines.add("");
        lines.add("###### Channel List ######");
        for (String channel : channels) {
            if (channel.length() >= 1) {
                lines.add(channel.substring(0, 1).equals(">") ? channel : ">" + channel);
            } else {
                lines.add("");
            }
        }

        String pathname = getPathName(project);
        Path path = Paths.get(pathname);
        try {
            Files.write(path, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean verify(Options options) {
        String message = null;
        if (StringUtil.isEmpty(options.keyStorePath)) {
            message = "Please choose a keystore file";
        } else if (StringUtil.isEmpty(options.keyStorePassword)) {
            message = "Please enter the keystore password";
        } else if (StringUtil.isEmpty(options.keyPassword)) {
            message = "Please enter the key password";
        } else if (StringUtil.isEmpty(options.keyAlias)) {
            message = "Please enter the key alias";
        } else if (options.channels.size() == 0) {
            message = "Please enter channels";
        } else if (StringUtil.isEmpty(options.zipalignPath)) {
            message = "Please choose a zipalign file";
        } else if (StringUtil.isEmpty(options.signer)) {
            message = "Please choose a signer";
        } else if (StringUtil.isEmpty(options.buildType)) {
            message = "Please choose a build type";
        }

        boolean success = message == null;
        if (!success) {
            NotificationHelper.error(message);
        }
        return success;
    }
}

package com.github.nukc.plugin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nukc.
 */
public class Options {
    public List<String> channels = new ArrayList<>(0);
    public String keyStorePath;
    public String keyStorePassword;
    public String keyAlias;
    public String keyPassword;
    public String zipalignPath;
    public String buildType;
    public String signer;
}

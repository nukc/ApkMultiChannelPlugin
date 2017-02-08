package com.github.nukc.plugin.helper;

import com.android.apksig.ApkVerifier;
import com.android.apksig.apk.ApkFormatException;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Nukc.
 */
public class ApkHelper {

    public static int checkSignatureVersion(File apkFile) throws ApkFormatException, NoSuchAlgorithmException, IOException {
        int version = 0;
        ApkVerifier.Result result = new ApkVerifier.Builder(apkFile)
                .setMinCheckedPlatformVersion(1)
                .setMaxCheckedPlatformVersion(Integer.MAX_VALUE)
                .build()
                .verify();
        if (result.isVerified()) {
            if (result.isVerifiedUsingV2Scheme()) {
                version = 2;
            } else if (result.isVerifiedUsingV1Scheme()){
                version = 1;
            }
        }
        return version;
    }

    public static boolean isV2Signature(File apkFile) throws ApkFormatException, NoSuchAlgorithmException, IOException {
        return checkSignatureVersion(apkFile) == 2;
    }

}

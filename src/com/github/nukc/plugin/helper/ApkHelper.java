package com.github.nukc.plugin.helper;

import com.android.apksigner.core.ApkVerifier;
import com.android.apksigner.core.internal.util.ByteBufferDataSource;
import com.android.apksigner.core.util.DataSource;
import com.android.apksigner.core.zip.ZipFormatException;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Nukc.
 */
public class ApkHelper {
    private static final Logger log = Logger.getInstance(ApkHelper.class);

    public static boolean checkV2Signature(File apkFile) throws IOException, ZipFormatException {
        boolean has = false;
        FileInputStream fIn = null;
        FileChannel fChan = null;
        try {
            fIn = new FileInputStream(apkFile);
            fChan = fIn.getChannel();
            long fSize = fChan.size();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) fSize);
            fChan.read(byteBuffer);
            byteBuffer.rewind();

            DataSource dataSource = new ByteBufferDataSource(byteBuffer);

            ApkVerifier apkVerifier = new ApkVerifier();
            ApkVerifier.Result result = apkVerifier.verify(dataSource, 0);
            if (!result.isVerified() || !result.isVerifiedUsingV2Scheme()) {
                log.warn("${apkFile} has no v2 signature in Apk Signing Block!");
            } else {
                has = true;
            }
        } finally {
            if (fChan != null) {
                fChan.close();
            }
            if (fIn != null) {
                fIn.close();
            }
        }
        return has;
    }
}

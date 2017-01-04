package com.github.nukc.plugin.helper;

import com.github.nukc.plugin.axml.ChannelEditor;
import com.github.nukc.plugin.axml.decode.AXMLDoc;
import com.github.nukc.plugin.model.Options;
import com.github.nukc.plugin.ui.OptionForm;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nukc.
 */
public class BuildHelper {
    private static final Logger log = Logger.getInstance(BuildHelper.class);

    public static void build(Project project, VirtualFile virtualFile) {
        if (virtualFile == null) return;

        Options options = OptionsHelper.load(project);
        if (options == null) {
            OptionForm.show(project, virtualFile);
            return;
        }
        if (!OptionsHelper.verify(options)) {
            OptionForm.show(project, virtualFile);
            return;
        }

        String apkNameWithoutExtension = virtualFile.getNameWithoutExtension();

        File apkFile = new File(virtualFile.getPath());
        File parentFile = apkFile.getParentFile();
        String tempPath = parentFile + File.separator + "temp";
        String outPath = parentFile + File.separator + "channels";

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Build Channel") {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                log.info("build type -> " + options.buildType);
                if (OptionsHelper.BUILD_TYPE_UPDATE.equals(options.buildType)) {
                    updateAndroidManifestXml(progressIndicator, virtualFile, tempPath, options,
                            apkNameWithoutExtension, outPath, apkFile);
                } else {
                    addChannelFileToMETAINF(progressIndicator, options, apkFile, outPath, tempPath,
                            apkNameWithoutExtension);
                }
            }
        });
    }

    private static void updateAndroidManifestXml(ProgressIndicator progressIndicator, VirtualFile virtualFile,
                                                 String tempPath, Options options, String apkNameWithoutExtension,
                                                 String outPath, File apkFile) {
        List<File> tempApkList = new ArrayList<>();
        try {
            ZipHelper.extractAndroidManifestXml(virtualFile.getPath(), tempPath);

            String xmlPath = tempPath + File.separator + "AndroidManifest.xml";
            AXMLDoc doc = new AXMLDoc();
            doc.parse(new FileInputStream(xmlPath));

            ChannelEditor editor = new ChannelEditor(doc);

            for (int i = 0, count = options.channels.size(); i < count; i++) {
                String channel = options.channels.get(i);
                progressIndicator.setText("Creating " + channel + " apk");
                progressIndicator.setText2(i + 1 + "/" + count);
                editor.setChannel(channel);
                editor.commit();

                File xmlFile = new File(xmlPath);
                doc.build(new FileOutputStream(xmlFile));

                String channelApkName = apkNameWithoutExtension + "-" + channel + "-unsigned";
                String tempApkPath = outPath + File.separator + channelApkName + ".apk";

                File tempApk = new File(tempApkPath);
                FileUtil.createIfDoesntExist(tempApk);
                tempApkList.add(tempApk);

                Map<String, File> relpathToFile = new HashMap<>();
                relpathToFile.put("AndroidManifest.xml", xmlFile);
                boolean success = ZipHelper.update(new FileInputStream(apkFile),
                        new FileOutputStream(tempApk),
                        relpathToFile);

                if (success) {
                    String apkPath = outPath + File.separator + apkNameWithoutExtension + "-" + channel + ".apk";
                    if ("jarsigner".equals(options.signer)) {
                        CommandHelper.execSigner(options, tempApkPath);
                        CommandHelper.execZipalign(options, tempApkPath, apkPath);
                    } else {
                        CommandHelper.execZipalign(options, tempApkPath, apkPath);
                        CommandHelper.execSigner(options, apkPath);
                    }
                } else {
                    FileUtil.delete(tempApk);
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            progressIndicator.setText("Build failed");
        } finally {
            progressIndicator.setText("Delete temp");
            for (File tempApk : tempApkList) {
                if (tempApk != null && tempApk.exists()) {
                    tempApk.delete();
                }
            }
            ZipHelper.deleteTemp(tempPath);

            progressIndicator.setFraction(1);
        }
    }

    private static void addChannelFileToMETAINF(ProgressIndicator progressIndicator, Options options,
                                                File apkFile, String outPath, String tempPath,
                                                String apkNameWithoutExtension) {
        for (int i = 0, count = options.channels.size(); i < count; i++) {
            String channel = options.channels.get(i);
            progressIndicator.setText("Creating " + channel + " apk");
            progressIndicator.setText2(i + 1 + "/" + count);

            try {
                String newApkPath = outPath + File.separator + apkNameWithoutExtension + "-" + channel + ".apk";
                File newApkFile = new File(newApkPath);
                FileUtil.createIfDoesntExist(newApkFile);

                FileUtil.copy(apkFile, newApkFile);
                boolean success = updateChannel(newApkPath, channel);
                if (!success) {
                    FileUtil.delete(newApkFile);
                }

                ZipHelper.deleteTemp(tempPath);
            } catch (IOException e) {
                e.printStackTrace();
                progressIndicator.setText("Build failed");
            }

        }

        progressIndicator.setFraction(1);
    }


    private static final String CHANNEL_FILE_PREFIX = "c_";
    /**
     * add channel file to META-INF
     */
    private static boolean updateChannel(String newApkPath, String channel) {
        Path path = Paths.get(newApkPath);
        URI uri = URI.create("jar:file:" + path.toUri().getPath());
        Map<String, String> env = new HashMap<>();

        try (FileSystem fileSystem =  FileSystems.newFileSystem(uri, env)){
            String relPath = "META-INF" + File.separator;
            final Path root = fileSystem.getPath(relPath);
            ChannelFileVisitor visitor = new ChannelFileVisitor();
            try {
                Files.walkFileTree(root, visitor);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("add channel failed：" + channel);
                return false;
            }

            Path existChannel = visitor.getChannelFile();
            if (existChannel != null) {
                Files.delete(existChannel);
                log.warn("the apk already exists channel：" + existChannel.getFileName().toString() + ", FilePath: " + newApkPath);
            }

            String relChannelPath = relPath + CHANNEL_FILE_PREFIX + channel;
            Path newChannel = fileSystem.getPath(relChannelPath);
            try {
                Files.createFile(newChannel);
            } catch (IOException e) {
                NotificationHelper.warn("add channel failed");
                log.error("add channel failed：" + channel);
                e.printStackTrace();
                return false;
            }

            return true;
        } catch (IOException e) {
            log.error("add channel failed：" + channel);
            e.printStackTrace();
            return false;
        }
    }

    private static class ChannelFileVisitor extends SimpleFileVisitor<Path> {
        private Path channelFile;

        public Path getChannelFile() {
            return channelFile;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.getFileName().toString().startsWith(CHANNEL_FILE_PREFIX)) {
                channelFile = file;
                return FileVisitResult.TERMINATE;
            } else {
                return FileVisitResult.CONTINUE;
            }
        }
    }
}

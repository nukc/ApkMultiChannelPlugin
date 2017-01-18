package com.github.nukc.plugin.ui;

import com.github.nukc.plugin.helper.BuildHelper;
import com.github.nukc.plugin.helper.OptionsHelper;
import com.github.nukc.plugin.model.Options;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uiDesigner.core.GridConstraints;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by Nukc.
 */
public class OptionForm extends JFrame {
    private JPanel mPanel;
    private JButton mBtnOk;
    private JButton mBtnCancel;
    private JPanel mPathJPanel;
    private JPasswordField mTextStorePwd;
    private JPasswordField mTextKeyPwd;
    private JTextField mTextAlias;
    private JTextArea mTextAreaChannels;
    private JPanel mZipalignJPanel;
    private JComboBox mCbBuildType;
    private JPanel mSignerJPanel;
    private JComboBox mCbSigner;
    private TextFieldWithBrowseButton mBtnPathBrowse;
    private TextFieldWithBrowseButton mBtnZipalignBrowse;
    private TextFieldWithBrowseButton mBtnSignerBrowse;

    private Project mProject;
    private VirtualFile mChooseFile;

    public static OptionForm show(Project project, VirtualFile chooseFile) {
        OptionForm optionForm = show(project);
        optionForm.setChooseFile(chooseFile);
        return optionForm;
    }

    public static OptionForm show(Project project) {
        OptionForm optionForm = new OptionForm(project);
        optionForm.setSize(450, 450);
        optionForm.setLocationRelativeTo(null);
        optionForm.setAlwaysOnTop(true);
        optionForm.setVisible(true);
        return optionForm;
    }

    public OptionForm(Project project) {
        setContentPane(mPanel);
        setTitle("Channel Build Setting");
        mProject = project;

        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        mBtnPathBrowse = new TextFieldWithBrowseButton();
        mBtnPathBrowse.addBrowseFolderListener("Choose Keystore File", null,
                mProject, fileChooserDescriptor);
        GridConstraints gridConstraints = new GridConstraints();
        gridConstraints.setFill(GridConstraints.FILL_BOTH);
        mPathJPanel.add(mBtnPathBrowse, gridConstraints);

        mBtnZipalignBrowse = new TextFieldWithBrowseButton();
        mBtnZipalignBrowse.addBrowseFolderListener("Choose Zipalign", null,
                null, fileChooserDescriptor);
        mZipalignJPanel.add(mBtnZipalignBrowse, gridConstraints);

        mBtnSignerBrowse = new TextFieldWithBrowseButton();
        mBtnSignerBrowse.addBrowseFolderListener("Choose ApkSigner", null,
                null, fileChooserDescriptor);
        mSignerJPanel.add(mBtnSignerBrowse, gridConstraints);
        mBtnSignerBrowse.setEnabled(false);

        mCbBuildType.addItem(OptionsHelper.BUILD_TYPE_UPDATE);
        mCbBuildType.addItem(OptionsHelper.BUILD_TYPE_ADD);
        mCbBuildType.addItem(OptionsHelper.BUILD_TYPE_ZIP_COMMENT);

        mBtnOk.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        mBtnCancel.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        mCbSigner.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int index = mCbSigner.getSelectedIndex();
                    if (index == 1) {
                        if ("jarsigner".equals(mBtnSignerBrowse.getText())) {
                            mBtnSignerBrowse.setText("");
                        }
                        mBtnSignerBrowse.setEnabled(true);
                    } else {
                        mBtnSignerBrowse.setEnabled(false);
                    }
                }
            }
        });

        mCbBuildType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String buildType = mCbBuildType.getSelectedItem().toString();
                    // v2 signature not support write zip comment
                    if (OptionsHelper.BUILD_TYPE_ZIP_COMMENT.equals(buildType)) {
                        mCbSigner.setEnabled(false);
                        mBtnSignerBrowse.setEnabled(false);
                    } else {
                        mCbSigner.setEnabled(true);
                        mBtnSignerBrowse.setEnabled(true);
                    }
                }
            }
        });

        Options options = OptionsHelper.load(mProject);
        if (options != null) {
            mBtnPathBrowse.setText(options.keyStorePath);
            mTextStorePwd.setText(options.keyStorePassword);
            mTextKeyPwd.setText(options.keyPassword);
            mTextAlias.setText(options.keyAlias);
            mBtnZipalignBrowse.setText(options.zipalignPath);
            mBtnSignerBrowse.setText(options.signer);
            if ("jarsigner".equals(options.signer)) {
                mCbSigner.setSelectedIndex(0);
            } else {
                mCbSigner.setSelectedIndex(1);
            }
            mCbBuildType.setSelectedItem(options.buildType);

            String text = "";
            for (String channel : options.channels) {
                text += ">" + channel + "\n";
            }
            mTextAreaChannels.setText(text);
        }
    }

    public void setChooseFile(VirtualFile virtualFile) {
        mChooseFile = virtualFile;
    }

    private void onOK() {
        dispose();

        String storePassword = String.valueOf(mTextStorePwd.getPassword());
        String storeFilePath = mBtnPathBrowse.getText();
        String keyPassword = String.valueOf(mTextKeyPwd.getPassword());
        String keyAlias = mTextAlias.getText();
        String[] channels = mTextAreaChannels.getText().split("\n");
        String zipalignPath = mBtnZipalignBrowse.getText();
        String buildType = mCbBuildType.getSelectedItem().toString();
        String signer = mCbSigner.getSelectedItem().toString();

        if ("apksigner".equals(signer)) {
            signer = mBtnSignerBrowse.getText();
        }

        System.out.print(storeFilePath + "--" + storePassword + "--" + keyPassword +
                "--" + keyAlias + "---" + signer + "---" + buildType);

        OptionsHelper.save(mProject, storeFilePath, storePassword, keyPassword, keyAlias,
                zipalignPath, channels, buildType, signer);

        BuildHelper.build(mProject, mChooseFile);
    }

    private void onCancel() {
        dispose();
    }

}

package com.github.nukc.plugin;

import com.github.nukc.plugin.ui.OptionForm;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

/**
 * Created by Nukc on 19/12/2016.
 */
public class SettingAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        OptionForm.show(project);
    }
}

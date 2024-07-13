package com.gbti.snapshotsforai;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SnapshotDialog extends DialogWrapper {
    private final List<JCheckBox> fileCheckBoxes = new ArrayList<>();
    private final JTextField promptField;
    private final JCheckBox includeEntireProjectStructureCheckBox;

    public SnapshotDialog(@Nullable Project project, String defaultPrompt, boolean defaultIncludeEntireProjectStructure) {
        super(project);
        setTitle("Create Snapshot");

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();

        for (VirtualFile file : openFiles) {
            if (!file.getPath().contains("/.snapshots/")) {
                JCheckBox checkBox = new JCheckBox(file.getPath());
                checkBox.setSelected(true); // Set checkbox as selected by default
                fileCheckBoxes.add(checkBox);
            }
        }

        promptField = new JTextField(defaultPrompt);
        includeEntireProjectStructureCheckBox = new JCheckBox("Include entire project structure");
        includeEntireProjectStructureCheckBox.setSelected(defaultIncludeEntireProjectStructure);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel filesPanel = new JPanel(new GridLayout(fileCheckBoxes.size(), 1));
        if (fileCheckBoxes.isEmpty()) {
            JLabel noFilesLabel = new JLabel("No files are currently open.");
            filesPanel.add(noFilesLabel);
        } else {
            for (JCheckBox checkBox : fileCheckBoxes) {
                if (checkBox != null) {
                    filesPanel.add(checkBox);
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(filesPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.add(includeEntireProjectStructureCheckBox, BorderLayout.NORTH);

        JPanel promptPanel = new JPanel(new BorderLayout());
        promptPanel.add(new JLabel("Prompt:"), BorderLayout.WEST);
        promptPanel.add(promptField, BorderLayout.CENTER);

        optionsPanel.add(promptPanel, BorderLayout.SOUTH);
        panel.add(optionsPanel, BorderLayout.NORTH);

        return panel;
    }

    public String getPrompt() {
        return promptField.getText();
    }

    public boolean isIncludeEntireProjectStructure() {
        return includeEntireProjectStructureCheckBox.isSelected();
    }

    public List<String> getSelectedFiles() {
        List<String> selectedFiles = new ArrayList<>();
        for (JCheckBox checkBox : fileCheckBoxes) {
            if (checkBox.isSelected()) {
                selectedFiles.add(checkBox.getText());
            }
        }
        return selectedFiles;
    }
}

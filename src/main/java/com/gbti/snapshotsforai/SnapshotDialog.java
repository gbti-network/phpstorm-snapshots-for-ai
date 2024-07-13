package com.gbti.snapshotsforai;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class SnapshotDialog extends DialogWrapper {
    private final List<JCheckBox> fileCheckBoxes = new ArrayList<>();
    private final JTextField promptField;
    private final JCheckBox includeEntireProjectStructureCheckBox;
    private final JCheckBox includeAllProjectFilesCheckBox;
    private final JPanel filesPanel;
    private final JLabel allFilesLabel;
    private final JScrollPane scrollPane;
    private final Project project;

    public SnapshotDialog(@Nullable Project project, String defaultPrompt, boolean defaultIncludeEntireProjectStructure) {
        super(project);
        this.project = project;
        setTitle("Generate Markdown Snapshot");

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

        includeAllProjectFilesCheckBox = new JCheckBox("Include all project files");
        includeAllProjectFilesCheckBox.addItemListener(e -> toggleFileCheckboxesVisibility());

        filesPanel = new JPanel(new GridLayout(fileCheckBoxes.size(), 1, 5, 5));
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

        allFilesLabel = new JLabel("<html><i>All applicable files located in the project will be included in the snapshot markdown.</i></html>");
        allFilesLabel.setVisible(false);

        scrollPane = new JScrollPane(filesPanel);

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(allFilesLabel, BorderLayout.SOUTH);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel promptPanel = new JPanel(new BorderLayout(5, 5));
        promptPanel.add(new JLabel("Prompt:"), BorderLayout.WEST);
        promptPanel.add(promptField, BorderLayout.CENTER);

        optionsPanel.add(promptPanel);
        optionsPanel.add(includeEntireProjectStructureCheckBox);
        optionsPanel.add(includeAllProjectFilesCheckBox);

        panel.add(optionsPanel, BorderLayout.NORTH);

        return panel;
    }

    private void toggleFileCheckboxesVisibility() {
        boolean includeAllFiles = includeAllProjectFilesCheckBox.isSelected();
        filesPanel.setVisible(!includeAllFiles);
        allFilesLabel.setVisible(includeAllFiles);
        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    public String getPrompt() {
        return promptField.getText();
    }

    public boolean isIncludeEntireProjectStructure() {
        return includeEntireProjectStructureCheckBox.isSelected();
    }

    public boolean isIncludeAllProjectFiles() {
        return includeAllProjectFilesCheckBox.isSelected();
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

    @Override
    protected Action[] createActions() {
        Action[] actions = new Action[3];
        actions[0] = getOKAction();
        actions[1] = getCancelAction();
        actions[2] = new AbstractAction("Edit Config") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editConfigFile();
            }
        };
        return actions;
    }

    private void editConfigFile() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            Messages.showErrorDialog("Project base path is null", "Snapshots for AI");
            return;
        }

        java.nio.file.Path configFilePath = Paths.get(basePath, ".snapshots", "config.json");
        if (!Files.exists(configFilePath)) {
            Messages.showErrorDialog("Config file not found. Please restart the IDE.", "Snapshots for AI");
            return;
        }

        VirtualFile configFile = VirtualFileManager.getInstance().refreshAndFindFileByUrl(configFilePath.toUri().toString());
        if (configFile != null) {
            FileEditorManager.getInstance(project).openFile(configFile, true);
        } else {
            Messages.showErrorDialog("Unable to open config file", "Snapshots for AI");
        }
    }
}

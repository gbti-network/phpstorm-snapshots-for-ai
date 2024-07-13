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
import java.awt.event.ItemEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final JButton toggleButton;

    public SnapshotDialog(@Nullable Project project, String defaultPrompt, boolean defaultIncludeEntireProjectStructure, boolean defaultIncludeAllFiles) {
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
        includeAllProjectFilesCheckBox.setSelected(defaultIncludeAllFiles);
        includeAllProjectFilesCheckBox.addItemListener(e -> toggleFileCheckboxesVisibility(e.getStateChange() == ItemEvent.SELECTED));

        toggleButton = new JButton("Select/Deselect All");
        toggleButton.setFont(toggleButton.getFont().deriveFont(Font.PLAIN, 10));
        toggleButton.addActionListener(this::toggleFileSelections);

        filesPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        if (fileCheckBoxes.isEmpty()) {
            JLabel noFilesLabel = new JLabel("No files are currently open.");
            filesPanel.add(noFilesLabel);
        } else {
            for (JCheckBox checkBox : fileCheckBoxes) {
                filesPanel.add(checkBox);
            }
        }

        allFilesLabel = new JLabel("<html><i>All applicable files located in the project will be included in the snapshot markdown.</i></html>");
        allFilesLabel.setVisible(defaultIncludeAllFiles);

        scrollPane = new JScrollPane(filesPanel);
        if (defaultIncludeAllFiles) {
            filesPanel.setVisible(false);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel promptPanel = new JPanel(new BorderLayout(5, 5));
        promptPanel.add(new JLabel("Prompt:"), BorderLayout.WEST);
        promptPanel.add(promptField, BorderLayout.CENTER);

        JPanel optionsPanel = new JPanel(new BorderLayout());
        JPanel optionsLeftPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        optionsLeftPanel.add(includeEntireProjectStructureCheckBox);

        JPanel includeAllPanel = new JPanel(new BorderLayout());
        includeAllPanel.add(includeAllProjectFilesCheckBox, BorderLayout.WEST);
        includeAllPanel.add(toggleButton, BorderLayout.EAST);

        optionsLeftPanel.add(includeAllPanel);

        optionsPanel.add(promptPanel, BorderLayout.NORTH);
        optionsPanel.add(optionsLeftPanel, BorderLayout.CENTER);

        panel.add(optionsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(allFilesLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void toggleFileCheckboxesVisibility(boolean includeAllFiles) {
        filesPanel.setVisible(!includeAllFiles);
        allFilesLabel.setVisible(includeAllFiles);

        if (includeAllFiles) {
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        } else {
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        }

        scrollPane.revalidate();
        scrollPane.repaint();
    }

    private void toggleFileSelections(ActionEvent e) {
        boolean selectAll = fileCheckBoxes.stream().anyMatch(checkBox -> !checkBox.isSelected());
        for (JCheckBox checkBox : fileCheckBoxes) {
            checkBox.setSelected(selectAll);
        }
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

        Path configFilePath = Paths.get(basePath, ".snapshots", "config.json");
        if (!Files.exists(configFilePath)) {
            Messages.showErrorDialog("Config file not found. Please restart the IDE.", "Snapshots for AI");
            return;
        }

        VirtualFile configFile = VirtualFileManager.getInstance().refreshAndFindFileByUrl(configFilePath.toUri().toString());
        if (configFile != null) {
            FileEditorManager.getInstance(project).openFile(configFile, true);
            close(DialogWrapper.CANCEL_EXIT_CODE);
        } else {
            Messages.showErrorDialog("Unable to open config file", "Snapshots for AI");
        }
    }
}

package com.gbti.snapshotsforai;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionManager;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SnapshotsToolWindowContent {
    private final JPanel myToolWindowContent;
    private final List<JCheckBox> fileCheckBoxes = new ArrayList<>();
    private final JTextField promptField;
    private final JCheckBox includeEntireProjectStructureCheckBox;
    private final Project project;

    public SnapshotsToolWindowContent(Project project) {
        this.project = project;
        myToolWindowContent = new SimpleToolWindowPanel(true, true);
        JPanel panel = new JBPanel<>(new BorderLayout());

        // Add a label
        panel.add(new JBLabel("Snapshots for AI", SwingConstants.CENTER), BorderLayout.NORTH);

        // Add components from SnapshotDialog
        JPanel filesPanel = new JPanel(new GridLayout(0, 1));
        JScrollPane scrollPane = new JScrollPane(filesPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        promptField = new JTextField("");
        includeEntireProjectStructureCheckBox = new JCheckBox("Include entire project structure");
        includeEntireProjectStructureCheckBox.setSelected(false);

        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.add(includeEntireProjectStructureCheckBox, BorderLayout.NORTH);

        JPanel promptPanel = new JPanel(new BorderLayout());
        promptPanel.add(new JLabel("Prompt:"), BorderLayout.WEST);
        promptPanel.add(promptField, BorderLayout.CENTER);

        optionsPanel.add(promptPanel, BorderLayout.SOUTH);
        panel.add(optionsPanel, BorderLayout.SOUTH);

        JButton generateButton = new JButton("Generate Markdown");
        generateButton.addActionListener(e -> generateMarkdown());
        panel.add(generateButton, BorderLayout.SOUTH);

        myToolWindowContent.add(panel, BorderLayout.CENTER);

        updateContent(project);
    }

    @NotNull
    public JComponent getContent() {
        return myToolWindowContent;
    }

    public void updateContent(Project project) {
        fileCheckBoxes.clear();
        JPanel filesPanel = (JPanel) ((JScrollPane) ((BorderLayout) myToolWindowContent.getLayout()).getLayoutComponent(BorderLayout.CENTER)).getViewport().getView();
        filesPanel.removeAll();

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();

        for (VirtualFile file : openFiles) {
            if (!file.getPath().contains("/.snapshots/")) {
                JCheckBox checkBox = new JCheckBox(file.getPath());
                checkBox.setSelected(true);
                fileCheckBoxes.add(checkBox);
                filesPanel.add(checkBox);
            }
        }

        filesPanel.revalidate();
        filesPanel.repaint();
    }

    private void generateMarkdown() {
        AnAction createSnapshotAction = AnActionManager.getInstance().getAction("com.gbti.snapshotsforai.actions.CreateSnapshotAction");
        if (createSnapshotAction != null) {
            AnActionEvent event = AnActionEvent.createFromAnAction(createSnapshotAction, null, "", new Presentation());
            createSnapshotAction.actionPerformed(event);
        }
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

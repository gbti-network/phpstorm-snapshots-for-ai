package com.gbti.snapshotsforai.actions;

import com.gbti.snapshotsforai.SnapshotsToolWindowFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

public class CreateSnapshotAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // Register listener to update tool window content on file open/close events
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                refreshToolWindowContent(project);
            }

            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                refreshToolWindowContent(project);
            }
        });

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

        String configContent;
        try {
            configContent = Files.readString(configFilePath);
        } catch (IOException ex) {
            Messages.showErrorDialog("Error reading config file: " + ex.getMessage(), "Snapshots for AI");
            return;
        }

        JSONObject config = new JSONObject(configContent);
        String defaultPrompt = config.optString("default_prompt", "");
        JSONArray excludedPatterns = config.optJSONArray("excluded_patterns");

        SnapshotsToolWindowFactory factory = new SnapshotsToolWindowFactory();
        factory.updateToolWindowContent(project);

        // Generate the markdown snapshot as before
        String prompt = factory.getToolWindowContent().getPrompt();
        boolean includeEntireProjectStructure = factory.getToolWindowContent().isIncludeEntireProjectStructure();
        List<String> selectedFiles = factory.getToolWindowContent().getSelectedFiles();

        if (includeEntireProjectStructure) {
            selectedFiles = getAllProjectFiles(basePath, excludedPatterns);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH_mm_ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String fileName = "snapshot-" + timestamp + ".md";

        StringBuilder markdown = new StringBuilder();
        markdown.append(prompt).append("\n\n");

        if (includeEntireProjectStructure) {
            markdown.append("# Project Structure\n\n");
            try {
                markdown.append(formatProjectStructure(basePath, selectedFiles));
            } catch (Exception ex) {
                markdown.append("Exception occurred while formatting project structure: ").append(ex.getMessage()).append("\n");
                for (StackTraceElement element : ex.getStackTrace()) {
                    markdown.append(element.toString()).append("\n");
                }
            }
            markdown.append("\n\n");
        }

        markdown.append("# Project Files\n\n");

        // Add a list of selected files
        for (String filePath : selectedFiles) {
            markdown.append("- ").append(filePath).append("\n");
        }

        markdown.append("\n");

        for (String filePath : selectedFiles) {
            markdown.append("## ").append(filePath).append("\n```\n");
            try {
                List<String> fileLines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
                for (String line : fileLines) {
                    markdown.append(line).append("\n");
                }
            } catch (IOException ex) {
                markdown.append("Error reading file: ").append(filePath).append(" - ").append(ex.getMessage()).append("\n");
                for (StackTraceElement element : ex.getStackTrace()) {
                    markdown.append(element.toString()).append("\n");
                }
            }
            markdown.append("```\n\n");
        }

        try {
            java.nio.file.Path snapshotsDir = Paths.get(basePath, ".snapshots");
            java.nio.file.Path snapshotFile = snapshotsDir.resolve(fileName);
            Files.write(snapshotFile, markdown.toString().getBytes(StandardCharsets.UTF_8));

            VirtualFile virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByUrl(snapshotFile.toUri().toString());
            if (virtualFile != null) {
                FileEditorManager.getInstance(project).openFile(virtualFile, true);
            }

            Messages.showInfoMessage("Snapshot created successfully!", "Snapshots for AI");
        } catch (IOException ex) {
            Messages.showErrorDialog("Error creating snapshot: " + ex.getMessage(), "Snapshots for AI");
        }
    }

    private void refreshToolWindowContent(Project project) {
        SnapshotsToolWindowFactory factory = new SnapshotsToolWindowFactory();
        factory.updateToolWindowContent(project);
    }

    private List<String> getAllProjectFiles(String basePath, JSONArray excludedPatterns) {
        List<String> fileList = new ArrayList<>();
        List<Pattern> patterns = new ArrayList<>();
        if (excludedPatterns != null) {
            for (int i = 0; i < excludedPatterns.length(); i++) {
                String patternStr = excludedPatterns.getString(i);
                patterns.add(Pattern.compile(".*" + Pattern.quote(patternStr) + "(/.*)?"));
            }
        }

        try {
            Files.walk(Paths.get(basePath))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String filePath = path.toString().replace('\\', '/');  // Normalize to use forward slashes
                    boolean excluded = patterns.stream().anyMatch(pattern -> pattern.matcher(filePath).matches());
                    if (!excluded) {
                        fileList.add(filePath);
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    private String formatProjectStructure(String basePath, List<String> filePaths) {
        StringBuilder structure = new StringBuilder();
        Path base = Paths.get(basePath);

        try {
            // First, create a tree structure for all the files
            DirectoryNode root = new DirectoryNode(base.toString());
            for (String filePath : filePaths) {
                Path relativePath = base.relativize(Paths.get(filePath));
                addPathToTree(root, relativePath);
            }

            // Then, convert the tree to a formatted string
            buildStructureString(structure, root, 0);
        } catch (Exception e) {
            structure.append("Exception occurred while building project structure: ").append(e.getMessage()).append("\n");
            for (StackTraceElement element : e.getStackTrace()) {
                structure.append(element.toString()).append("\n");
            }
        }

        return structure.toString();
    }

    private void addPathToTree(DirectoryNode currentNode, Path relativePath) throws Exception {
        if (relativePath.getNameCount() == 0) return;

        String part = relativePath.getName(0).toString();
        DirectoryNode childNode = currentNode.getChild(part);
        if (childNode == null) {
            childNode = new DirectoryNode(part);
            currentNode.addChild(childNode);
        }

        if (relativePath.getNameCount() > 1) {
            addPathToTree(childNode, relativePath.subpath(1, relativePath.getNameCount()));
        }
    }

    private void buildStructureString(StringBuilder structure, DirectoryNode node, int level) {
        if (level > 0) {
            structure.append("    ".repeat(level - 1))
                     .append(node.isDirectory() ? "+ " : "- ")
                     .append(node.getName())
                     .append("\n");
        }

        for (DirectoryNode child : node.getChildren()) {
            buildStructureString(structure, child, level + 1);
        }
    }

    private class DirectoryNode {
        private final String name;
        private final List<DirectoryNode> children;

        public DirectoryNode(String name) {
            this.name = name;
            this.children = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public boolean isDirectory() {
            return !children.isEmpty();
        }

        public List<DirectoryNode> getChildren() {
            return children;
        }

        public void addChild(DirectoryNode child) {
            children.add(child);
        }

        public DirectoryNode getChild(String name) {
            for (DirectoryNode child : children) {
                if (child.getName().equals(name)) {
                    return child;
                }
            }
            return null;
        }
    }
}

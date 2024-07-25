package com.gbti.snapshotsforai.actions;

import com.gbti.snapshotsforai.SnapshotDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CreateSnapshotAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

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

        String configContent;
        try {
            configContent = Files.readString(configFilePath);
        } catch (IOException ex) {
            Messages.showErrorDialog("Error reading config file: " + ex.getMessage(), "Snapshots for AI");
            return;
        }

        JSONObject config = new JSONObject(configContent);
        JSONObject defaultConfig = config.optJSONObject("default");
        String defaultPrompt = defaultConfig.optString("default_prompt", "");
        boolean defaultIncludeEntireProjectStructure = defaultConfig.optBoolean("default_include_entire_project_structure", false);
        boolean defaultIncludeAllFiles = defaultConfig.optBoolean("default_include_all_files", false);
        JSONArray excludedPatterns = config.optJSONArray("excluded_patterns");
        JSONArray includedPatterns = config.optJSONArray("included_patterns");

        SnapshotDialog dialog = new SnapshotDialog(project, defaultPrompt, defaultIncludeEntireProjectStructure, defaultIncludeAllFiles);
        if (!dialog.showAndGet()) {
            return;
        }

        String prompt = dialog.getPrompt();
        boolean includeEntireProjectStructure = dialog.isIncludeEntireProjectStructure();
        boolean includeAllFiles = dialog.isIncludeAllProjectFiles();
        List<String> selectedFiles = dialog.getSelectedFiles();

        // If "Include all project files" is checked, get all project files not excluded by patterns
        if (includeAllFiles) {
            selectedFiles = getAllProjectFiles(basePath, excludedPatterns, includedPatterns);
        }

        // Filter out image files except SVGs
        selectedFiles = filterOutImageFiles(selectedFiles);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH_mm_ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String fileName = "snapshot-" + timestamp + ".md";

        StringBuilder markdown = new StringBuilder();
        markdown.append(prompt).append("\n\n");

        // Add project structure if "Include entire project structure" is checked
        if (includeEntireProjectStructure) {
            markdown.append("# Project Structure\n\n");
            try {
                List<String> projectFiles = getAllProjectFiles(basePath, excludedPatterns, includedPatterns);
                markdown.append(formatProjectStructure(basePath, projectFiles));
            } catch (Exception ex) {
                markdown.append("Exception occurred while formatting project structure: ").append(ex.getMessage()).append("\n");
                for (StackTraceElement element : ex.getStackTrace()) {
                    markdown.append(element.toString()).append("\n");
                }
            }
            markdown.append("\n\n");
        }

        // Add selected or all files in "# Project Files"
        markdown.append("# Project Files\n\n");
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
            Path snapshotsDir = Paths.get(basePath, ".snapshots");
            if (!Files.exists(snapshotsDir)) {
                Files.createDirectories(snapshotsDir);
            }

            Path snapshotFile = snapshotsDir.resolve(fileName);
            Files.write(snapshotFile, markdown.toString().getBytes(StandardCharsets.UTF_8));

            // Refresh the snapshots directory to ensure the new file is visible
            VirtualFile snapshotsVirtualDir = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(snapshotsDir);
            if (snapshotsVirtualDir != null) {
                snapshotsVirtualDir.refresh(false, true);
            }

            // Refresh and open the snapshot file in the editor
            VirtualFile virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(snapshotFile);
            if (virtualFile != null) {
                virtualFile.refresh(false, false);
                FileEditorManager.getInstance(project).openFile(virtualFile, true);
            }

            Messages.showInfoMessage("Snapshot created successfully!", "Snapshots for AI");
        } catch (IOException ex) {
            Messages.showErrorDialog("Error creating snapshot: " + ex.getMessage(), "Snapshots for AI");
        }
    }

    private List<String> getAllProjectFiles(String basePath, JSONArray excludedPatterns, JSONArray includedPatterns) {
        List<String> fileList = new ArrayList<>();
        List<Pattern> excludePatterns = new ArrayList<>();
        List<Pattern> includePatterns = new ArrayList<>();

        if (excludedPatterns != null) {
            for (int i = 0; i < excludedPatterns.length(); i++) {
                String patternStr = excludedPatterns.getString(i);
                patternStr = Pattern.quote(patternStr).replace("*", "\\E.*\\Q");
                excludePatterns.add(Pattern.compile("^" + Pattern.quote(basePath.replace("\\", "/")) + "/\\.?" + patternStr + ".*$"));
            }
        }

        if (includedPatterns != null) {
            for (int i = 0; i < includedPatterns.length(); i++) {
                String patternStr = includedPatterns.getString(i);
                patternStr = patternStr.replace(".", "\\.").replace("*", ".*");
                includePatterns.add(Pattern.compile(".*/" + patternStr + "$"));
            }
        }

        try {
            Files.walk(Paths.get(basePath))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String filePath = path.toString().replace('\\', '/');
                    boolean excluded = isExcluded(filePath, excludePatterns);
                    boolean included = isIncluded(filePath, includePatterns);
                    boolean inExcludedDir = isInExcludedDirectory(filePath, excludePatterns);

                    if (included && !inExcludedDir) {
                        fileList.add(filePath);
                    } else if (!excluded && !inExcludedDir) {
                        fileList.add(filePath);
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    private boolean isExcluded(String filePath, List<Pattern> excludePatterns) {
        for (Pattern pattern : excludePatterns) {
            if (pattern.matcher(filePath).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean isIncluded(String filePath, List<Pattern> includePatterns) {
        if (includePatterns.isEmpty()) {
            return true; // If no include patterns, consider all files included
        }
        for (Pattern pattern : includePatterns) {
            if (pattern.matcher(filePath).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInExcludedDirectory(String filePath, List<Pattern> excludePatterns) {
        Path path = Paths.get(filePath).getParent();
        while (path != null) {
            if (isExcluded(path.toString().replace('\\', '/'), excludePatterns)) {
                return true;
            }
            path = path.getParent();
        }
        return false;
    }

    private List<String> filterOutImageFiles(List<String> filePaths) {
        List<String> filteredFiles = new ArrayList<>();
        for (String filePath : filePaths) {
            if (!filePath.matches(".*\\.(jpg|jpeg|png|gif|bmp|tiff)$") || filePath.endsWith(".svg")) {
                filteredFiles.add(filePath);
            }
        }
        return filteredFiles;
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
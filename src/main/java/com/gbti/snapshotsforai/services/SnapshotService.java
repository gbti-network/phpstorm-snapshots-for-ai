package com.gbti.snapshotsforai;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public final class SnapshotService {
    private final Project project;

    public SnapshotService(Project project) {
        this.project = project;
    }

    public void initializeSnapshotDirectory() throws IOException {
        String basePath = project.getBasePath();
        if (basePath == null) {
            throw new IOException("Project base path is null");
        }

        Path snapshotsDir = Paths.get(basePath, ".snapshots");
        if (!Files.exists(snapshotsDir)) {
            Files.createDirectory(snapshotsDir);
        }

        Path configFilePath = snapshotsDir.resolve("config.json");
        JSONObject config;

        if (Files.exists(configFilePath)) {
            String configContent = new String(Files.readAllBytes(configFilePath));
            config = new JSONObject(configContent);
        } else {
            config = new JSONObject();
        }

        // Set default values if they are not already present
        if (!config.has("excluded_patterns")) {
            config.put("excluded_patterns", new JSONArray()
                .put(".git")
                .put(".gitignore")
                .put("gradlew")
                .put("gradlew.*")
                .put("node_modules")
                .put(".snapshots")
                .put(".idea")
                .put(".vscode")
                .put("gradle")
                .put("*.log")
                .put("*.tmp")
                .put("target")
                .put("dist")
                .put("build")
                .put(".DS_Store")
                .put("*.bak")
                .put("*.swp")
                .put("*.swo")
                .put("*.lock")
                .put("*.iml")
                .put("coverage")
                .put("*.min.js")
                .put("*.min.css")
                .put("__pycache__")
            );
        }

        JSONObject defaultConfig = config.optJSONObject("default");
        if (defaultConfig == null) {
            defaultConfig = new JSONObject();
            config.put("default", defaultConfig);
        }

        if (!defaultConfig.has("default_prompt")) {
            defaultConfig.put("default_prompt", "Enter your prompt here");
        }
        if (!defaultConfig.has("default_include_entire_project_structure")) {
            defaultConfig.put("default_include_entire_project_structure", true);
        }
        if (!defaultConfig.has("default_include_all_files")) {
            defaultConfig.put("default_include_all_files", false);
        }

        Files.write(configFilePath, config.toString(4).getBytes());

        Path readmeFilePath = snapshotsDir.resolve("readme.md");
        String readmeContent = "# Snapshots for AI\n\n" +
                "## Configuration\n\n" +
                "The `config.json` file allows you to customize the behavior of the Snapshots for AI plugin.\n\n" +
                "### Options\n\n" +
                "- `excluded_patterns`: A list of patterns to exclude from the project structure snapshot. Patterns include:\n" +
                "  - `.git`\n" +
                "  - `.gitignore`\n" +
                "  - `gradlew`\n" +
                "  - `gradlew.*`\n" +
                "  - `node_modules`\n" +
                "  - `.snapshots`\n" +
                "  - `.idea`\n" +
                "  - `.vscode`\n" +
                "  - `gradle`\n" +
                "  - `*.log`\n" +
                "  - `*.tmp`\n" +
                "  - `target`\n" +
                "  - `dist`\n" +
                "  - `build`\n" +
                "  - `.DS_Store`\n" +
                "  - `*.bak`\n" +
                "  - `*.swp`\n" +
                "  - `*.swo`\n" +
                "  - `*.lock`\n" +
                "  - `*.iml`\n" +
                "  - `coverage`\n" +
                "  - `*.min.js`\n" +
                "  - `*.min.css`\n" +
                "  - `__pycache__`\n" +
                "- `default`: A subkey containing default settings:\n" +
                "  - `default_prompt`: A default prompt to be used when creating snapshots.\n" +
                "  - `default_include_entire_project_structure`: A boolean setting to include the entire project structure by default.\n" +
                "  - `default_include_all_files`: A boolean setting to include all project files by default.\n\n" +
                "## Usage\n\n" +
                "To create a snapshot, follow these steps:\n\n" +
                "### From the Tools Menu\n" +
                "1. Open the `Tools` menu in PHPStorm.\n" +
                "2. Select `Create Snapshot`.\n" +
                "3. Enter your prompt (if not using the default prompt).\n" +
                "4. Select the files to include in the snapshot.\n" +
                "5. Click `OK` to generate the snapshot.\n\n" +
                "### From the Main Toolbar\n" +
                "1. Click on the `Create Snapshot` icon in the main toolbar.\n" +
                "2. Follow the same steps as above to create a snapshot.\n\n" +
                "The snapshot will be saved in the `.snapshots` directory within your project.\n";

        Files.write(readmeFilePath, readmeContent.getBytes());

        VirtualFileManager.getInstance().asyncRefresh(() -> {
            System.out.println("Virtual file system refreshed");
        });
    }
}

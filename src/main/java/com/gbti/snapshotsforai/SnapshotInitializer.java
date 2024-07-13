package com.gbti.snapshotsforai;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SnapshotInitializer implements ProjectComponent {
    private final Project project;

    public SnapshotInitializer(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        try {
            initializeSnapshotDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeSnapshotDirectory() throws IOException {
        String basePath = project.getBasePath();
        if (basePath == null) {
            throw new IOException("Project base path is null");
        }

        Path snapshotsDir = Paths.get(basePath, ".snapshots");
        if (!Files.exists(snapshotsDir)) {
            Files.createDirectory(snapshotsDir);
        }

        Path configFilePath = snapshotsDir.resolve("config.json");
        if (!Files.exists(configFilePath)) {
            String configJson = "{\n" +
                "    \"default_prompt\": \"Enter your prompt here\",\n" +
                "    \"excluded_patterns\": [\n" +
                "        \".git\",\n" +
                "        \"node_modules\",\n" +
                "        \".snapshots\",\n" +
                "        \".idea\",\n" +
                "        \".vscode\",\n" +
                "        \"gradle\",\n" +
                "        \"*.log\",\n" +
                "        \"*.tmp\",\n" +
                "        \"target\",\n" +
                "        \"dist\",\n" +
                "        \"build\",\n" +
                "        \"*.class\",\n" +
                "        \".DS_Store\",\n" +
                "        \"*.bak\",\n" +
                "        \"*.swp\",\n" +
                "        \"*.swo\",\n" +
                "        \"*.lock\",\n" +
                "        \"*.iml\",\n" +
                "        \"coverage\",\n" +
                "        \"*.min.js\",\n" +
                "        \"*.min.css\"\n" +
                "    ]\n" +
                "}\n";
            Files.write(configFilePath, configJson.getBytes());
        }

        Path readmeFilePath = snapshotsDir.resolve("readme.md");
        if (!Files.exists(readmeFilePath)) {
            String readmeContent = "# Snapshots for AI\n\n" +
                "## Configuration\n\n" +
                "The `config.json` file allows you to customize the behavior of the Snapshots for AI plugin.\n\n" +
                "### Options\n\n" +
                "- `default_prompt`: A default prompt to be used when creating snapshots.\n" +
                "- `excluded_patterns`: A list of patterns to exclude from the project structure snapshot. Patterns include:\n" +
                "  - `.git`\n" +
                "  - `.gradle`\n" +
                "  - `node_modules`\n" +
                "  - `.snapshots`\n" +
                "  - `.idea`\n" +
                "  - `.vscode`\n" +
                "  - `*.log`\n" +
                "  - `*.tmp`\n" +
                "  - `target`\n" +
                "  - `dist`\n" +
                "  - `build`\n" +
                "  - `*.class`\n" +
                "  - `.DS_Store`\n" +
                "  - `*.bak`\n" +
                "  - `*.swp`\n" +
                "  - `*.swo`\n" +
                "  - `*.lock`\n" +
                "  - `*.iml`\n" +
                "  - `coverage`\n" +
                "  - `*.min.js`\n" +
                "  - `*.min.css`\n\n" +
                "## Usage\n\n" +
                "To create a snapshot, follow these steps:\n\n" +
                "1. Open the `Tools` menu in PHPStorm.\n" +
                "2. Select `Create Snapshot`.\n" +
                "3. Enter your prompt (if not using the default prompt).\n" +
                "4. Select the files to include in the snapshot.\n" +
                "5. Click `OK` to generate the snapshot.\n\n" +
                "The snapshot will be saved in the `.snapshots` directory within your project.\n";
            Files.write(readmeFilePath, readmeContent.getBytes());
        }

        VirtualFileManager.getInstance().syncRefresh();
    }
}

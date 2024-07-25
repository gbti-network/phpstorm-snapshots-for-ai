package com.gbti.snapshotsforai.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
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
    private static final Logger LOG = Logger.getInstance(SnapshotService.class);
    private final Project project;

    public SnapshotService(Project project) {
        this.project = project;
    }

    public void initializeSnapshotDirectory() throws IOException {
        LOG.info("Initializing snapshot directory");
        String basePath = project.getBasePath();
        if (basePath == null) {
            LOG.error("Project base path is null");
            throw new IOException("Project base path is null");
        }
        LOG.info("Project base path: " + basePath);

        Path snapshotsDir = Paths.get(basePath, ".snapshots");
        LOG.info("Snapshots directory path: " + snapshotsDir);

        if (!Files.exists(snapshotsDir)) {
            LOG.info("Creating snapshots directory");
            Files.createDirectory(snapshotsDir);
        } else {
            LOG.info("Snapshots directory already exists");
        }

        Path configFilePath = snapshotsDir.resolve("config.json");
        if (!Files.exists(configFilePath)) {
            LOG.info("Creating config file at: " + configFilePath);
            JSONObject config = new JSONObject();
            config.put("excluded_patterns", new JSONArray()
                .put(".git")
                .put(".gitignore")
                .put("gradle")
                .put("gradlew")
                .put("gradlew.*")
                .put("node_modules")
                .put(".snapshots")
                .put(".idea")
                .put(".vscode")
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

            config.put("included_patterns", new JSONArray()
                .put("build.gradle")
                .put("settings.gradle")
                .put("gradle.properties")
                .put("pom.xml")
                .put("Makefile")
                .put("CMakeLists.txt")
                .put("package.json")
                .put("package-lock.json")
                .put("yarn.lock")
                .put("requirements.txt")
                .put("Pipfile")
                .put("Pipfile.lock")
                .put("Gemfile")
                .put("Gemfile.lock")
                .put("composer.json")
                .put("composer.lock")
                .put(".editorconfig")
                .put(".eslintrc.json")
                .put(".eslintrc.js")
                .put(".prettierrc")
                .put(".babelrc")
                .put(".env")
                .put(".dockerignore")
                .put(".gitattributes")
                .put(".stylelintrc")
                .put(".npmrc")
            );

            JSONObject defaultConfig = new JSONObject();
            defaultConfig.put("default_prompt", "Enter your prompt here");
            defaultConfig.put("default_include_entire_project_structure", true);
            defaultConfig.put("default_include_all_files", false);

            config.put("default", defaultConfig);
            Files.write(configFilePath, config.toString(4).getBytes());
        } else {
            LOG.info("Config file already exists at: " + configFilePath);
        }

        Path readmeFilePath = snapshotsDir.resolve("readme.md");
        String readmeContent = "# Snapshots for AI\n\n" +
                "## Configuration\n\n" +
                "The `config.json` file allows you to customize the behavior of the Snapshots for AI plugin.\n\n" +
                "### Options\n\n" +
                "- `excluded_patterns`: A list of patterns to exclude from the project structure snapshot. Patterns include:\n" +
                "  - `.git`\n" +
                "  - `.gitignore`\n" +
                "  - `gradle`\n" +
                "  - `gradlew`\n" +
                "  - `gradlew.*`\n" +
                "  - `node_modules`\n" +
                "  - `.snapshots`\n" +
                "  - `.idea`\n" +
                "  - `.vscode`\n" +
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
                "- `included_patterns`: A list of patterns to include in the project structure snapshot. Patterns include:\n" +
                "  - `build.gradle`\n" +
                "  - `settings.gradle`\n" +
                "  - `gradle.properties`\n" +
                "  - `pom.xml`\n" +
                "  - `Makefile`\n" +
                "  - `CMakeLists.txt`\n" +
                "  - `package.json`\n" +
                "  - `package-lock.json`\n" +
                "  - `yarn.lock`\n" +
                "  - `requirements.txt`\n" +
                "  - `Pipfile`\n" +
                "  - `Pipfile.lock`\n" +
                "  - `Gemfile`\n" +
                "  - `Gemfile.lock`\n" +
                "  - `composer.json`\n" +
                "  - `composer.lock`\n" +
                "  - `.editorconfig`\n" +
                "  - `.eslintrc.json`\n" +
                "  - `.eslintrc.js`\n" +
                "  - `.prettierrc`\n" +
                "  - `.babelrc`\n" +
                "  - `.env`\n" +
                "  - `.dockerignore`\n" +
                "  - `.gitattributes`\n" +
                "  - `.stylelintrc`\n" +
                "  - `.npmrc`\n\n" +
                "## Default Configuration\n\n" +
                "- `default_prompt`: The default prompt text that will be displayed in the snapshot dialog.\n" +
                "- `default_include_entire_project_structure`: Whether to include the entire project structure by default when creating a snapshot.\n" +
                "- `default_include_all_files`: Whether to include all project files by default when creating a snapshot.\n" +
                "\n" +
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
            LOG.info("Virtual file system refreshed");
        });
    }
}

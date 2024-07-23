# Snapshots for AI

https://plugins.jetbrains.com/plugin/24889-snapshots-for-ai/

## Overview

Snapshots for AI is a custom plugin for PHPStorm that generates machine-readable markdown snapshots of the files the user is currently working on. These generated markdown files can be fed to your favorite large language model (LLM) for further processing or analysis.

### Video Introduction

https://www.youtube.com/watch?v=xiCwXUZVW6k

## Features

- **Generate Markdown Snapshots**: Create snapshots of your current files in markdown format.
- **Include Project Structure**: Optionally include the entire project structure in the snapshot.
- **Selective File Inclusion**: Choose specific files or include all files from the project in the snapshot.
- **Configurable Exclusions**: Exclude specific file patterns from snapshots using the configuration file.

## Installation

1. Download and install the plugin from the [JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/24889-snapshots-for-ai/).

2. Open a project in PHPStorm.

3. Find the plugin and activate the free trial:
   - Navigate to `File > Settings > Plugins`.
   - Search for "Snapshots for AI".
   - Click on the plugin and select `Install`.
   - After installation, click on `Activate Free Trial`.

## Usage

1. **Creating a Snapshot**:
   - Navigate to `Tools > Create Snapshot` or click the camera icon at the top right side of the main toolbar.
   - Enter your prompt, select the files to include, and click `OK`.
   - Snapshop will be generated and opened automatically.
   - Snapshots are stored inside the `.snapshots` directory at the root of your project application

### Screenshots

![image](https://github.com/user-attachments/assets/e71f10db-b520-49c0-a371-1fdfa604f362)
![image](https://github.com/user-attachments/assets/bfbaa1f3-6e25-4c82-bd7f-648e42c1e7ee)
![image](https://github.com/user-attachments/assets/747bda18-8573-4573-8839-2ef7d21d61eb)
### Example Markdown Output

https://plugins.jetbrains.com/plugin/24889-snapshots-for-ai/example-markdown-output

## Configuration

The `config.json` file in the `.snapshots` directory allows you to customize the plugin's behavior.

### Default Configuration

```json
{
  "default": {
    "default_prompt": "Enter your prompt here",
    "default_include_entire_project_structure": true,
    "default_include_all_files": false
  },
  "excluded_patterns": [
    ".git",
    ".gitignore",
    "gradlew",
    "gradlew.*",
    "node_modules",
    ".snapshots",
    ".idea",
    ".vscode",
    "gradle",
    "*.log",
    "*.tmp",
    "target",
    "dist",
    "build",
    ".DS_Store",
    "*.bak",
    "*.swp",
    "*.swo",
    "*.lock",
    "*.iml",
    "coverage",
    "*.min.js",
    "*.min.css",
    "__pycache__"
  ],
  "included_patterns": [
    "build.gradle",
    "settings.gradle",
    "gradle.properties",
    "pom.xml",
    "Makefile",
    "CMakeLists.txt",
    "package.json",
    "package-lock.json",
    "yarn.lock",
    "requirements.txt",
    "Pipfile",
    "Pipfile.lock",
    "Gemfile",
    "Gemfile.lock",
    "composer.json",
    "composer.lock",
    ".editorconfig",
    ".eslintrc.json",
    ".eslintrc.js",
    ".prettierrc",
    ".babelrc",
    ".env",
    ".dockerignore",
    ".gitattributes",
    ".stylelintrc",
    ".npmrc"
  ]
}

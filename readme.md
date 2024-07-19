# Snapshots for AI

## Overview

Snapshots for AI is a custom plugin for PHPStorm that generates machine-readable markdown snapshots of the files the user is currently working on. These generated markdown files can be fed to your favorite large language model (LLM) for further processing or analysis.

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
    - Navigate to `Tools > Create Snapshot`.
    - Enter your prompt, select the files to include, and click `OK`.

2. **Opening the Snapshots Tool Window**:
    - Click on the `Snapshots for AI` tool window in the right sidebar of PHPStorm.

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
    "*.min.css"
  ]
}
```

## Development

To contribute to the development of this plugin, follow these steps:

1. **Fork the repository** on GitHub.

2. **Create a new branch**:
   ```bash
   git checkout -b feature-branch-name
   ```

3. **Make your changes** and commit them:
   ```bash
   git commit -m "Description of changes"
   ```

4. **Push to your forked repository**:
   ```bash
   git push origin feature-branch-name
   ```

5. **Create a pull request** on GitHub.

Note, to 

## Contact

For any inquiries or support, please contact [gbti.labs@gmail.com](mailto:gbti.labs@gmail.com).

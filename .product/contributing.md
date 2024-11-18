# Contributing to Snapshots for AI

Thank you for your interest in contributing to Snapshots for AI! We welcome contributions from the community to help make this plugin even better. This document outlines the process for contributing to the project.

## Getting Started

### Important: Always Start with an Issue

Before making any changes or submitting a pull request:
1. **Check Existing Issues**
    - Search through existing issues to avoid duplicates
    - Review open pull requests to ensure no one is already working on the same thing

2. **Create an Issue First**
    - Create a new issue describing what you want to work on
    - Explain the problem you're solving or feature you're adding
    - Wait for maintainer feedback and approval
    - This ensures your time is well spent and your contribution aligns with project goals

3. **Reference the Issue**
    - All pull requests must be linked to an issue
    - Use keywords like "Fixes #123" or "Resolves #123" in your PR description
    - This helps maintain traceability and context for changes

## Development Process

1. **Fork the Repository**
    - Visit the [Snapshots for AI repository](https://github.com/gbti-network/phpstorm-snapshots-for-ai)
    - Click the "Fork" button in the top-right corner
    - This will create a copy of the repository in your GitHub account

2. **Clone Your Fork**
   ```bash
   git clone https://github.com/gbti-network/phpstorm-snapshots-for-ai.git
   cd phpstorm-snapshots-for-ai
   ```

3. **Set Up Development Environment**
    - Install Java Development Kit (JDK) 17
    - Install IntelliJ IDEA or PhpStorm
    - Import the project as a Gradle project
    - Ensure all dependencies are properly resolved

## Making Changes

1. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b bugfix/your-bugfix-name
   ```

2. **Development Guidelines**
    - Follow the existing code style and conventions
    - Keep your changes focused and atomic
    - Write meaningful commit messages
    - Add appropriate comments and documentation
    - Update the changelog.md file with your changes
    - Test your changes thoroughly

3. **Running Tests**

No tests exist at this time. 

   ```bash
   ./gradlew test
   ```

4. **Building the Plugin**
   ```bash
   ./gradlew buildPlugin
   ```

## Submitting Changes

1. **Push Your Changes**
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create a Pull Request**
    - Go to your fork on GitHub
    - Click "New Pull Request"
    - Select your feature branch
    - Fill out the PR template with:
        - A clear title and description
        - Reference to the related issue (required)
        - Description of the changes made
        - Any additional context or screenshots

3. **Code Review Process**
    - Maintainers will review your PR
    - Address any feedback or requested changes
    - Once approved, your PR will be merged

## What to Contribute

- Bug fixes
- New features
- Documentation improvements
- Performance optimizations
- Additional file pattern configurations
- UI/UX enhancements

## Best Practices

- Always start with an issue
- Keep PRs small and focused
- Test your changes thoroughly
- Update documentation as needed
- Follow semantic versioning
- Keep the changelog up to date
- Write clear commit messages
- Respond to review comments promptly

## Getting Help

If you need help with anything:
- Open an issue with your question
- Join our community discussions
- Reach out to the maintainers

## Code of Conduct

Please note that this project is released with a Contributor Code of Conduct. By participating in this project you agree to abide by its terms.

## License

By contributing to Snapshots for AI, you agree that your contributions will be licensed under the same terms as the original project.

## Thank You

Your contributions to Snapshots for AI are greatly appreciated. Thank you for helping make this project better for everyone!
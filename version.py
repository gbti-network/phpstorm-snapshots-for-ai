import re
from datetime import datetime
from pathlib import Path

def get_next_version(current_version):
    parts = current_version.split('.')
    if len(parts) == 3:
        parts[2] = str(int(parts[2]) + 1)
    elif len(parts) == 2:
        parts[1] = str(int(parts[1]) + 1)
    return '.'.join(parts)

def extract_release_version(version):
    parts = version.split('.')
    if len(parts) >= 2:
        return f"{parts[0]}{parts[1]}"
    else:
        raise ValueError("Version format is incorrect")

def update_plugin_xml(file_path, release_date, release_version, version):
    with open(file_path, 'r') as file:
        content = file.read()

    # Update release-date
    content = re.sub(r'release-date="(\d+)"', f'release-date="{release_date}"', content)
    # Update release-version
    content = re.sub(r'release-version="(\d+)"', f'release-version="{release_version}"', content)
    # Update version
    content = re.sub(r'<version>([^<]+)</version>', f'<version>{version}</version>', content)

    with open(file_path, 'w') as file:
        file.write(content)
    print(f'Updated {file_path}')

def update_build_gradle(file_path, old_version, new_version):
    with open(file_path, 'r') as file:
        content = file.read()

    # Debugging: Print the content of build.gradle before modification
    print("Current build.gradle content:")
    print(content)

    # Replace old version with new version
    new_content = content.replace(old_version, new_version)

    # Debugging: Print the new content of build.gradle after modification
    print("New build.gradle content:")
    print(new_content)

    if content != new_content:
        with open(file_path, 'w') as file:
            file.write(new_content)
        print(f'Updated {file_path}')
    else:
        print(f'No changes made to {file_path}')

def get_current_version(file_path):
    with open(file_path, 'r') as file:
        content = file.read()

    match = re.search(r'<version>([^<]+)</version>', content)
    if match:
        return match.group(1)
    else:
        raise ValueError("Current version not found in plugin.xml")

def get_current_release_version(file_path):
    with open(file_path, 'r') as file:
        content = file.read()

    match = re.search(r'release-version="(\d+)"', content)
    if match:
        return match.group(1)
    else:
        raise ValueError("Current release-version not found in plugin.xml")

if __name__ == "__main__":
    # Automatically detect the base path
    base_path = Path(__file__).resolve().parent
    plugin_xml_path = base_path / "src/main/resources/META-INF/plugin.xml"
    build_gradle_path = base_path / "build.gradle"

    current_version = get_current_version(plugin_xml_path)
    current_release_version = get_current_release_version(plugin_xml_path)
    print(f"Current version: {current_version}")
    print(f"Current release-version: {current_release_version}")

    proposed_version = get_next_version(current_version)
    print(f"Proposed next version: {proposed_version}")

    new_version = input(f"Enter the new version (default: {proposed_version}): ") or proposed_version

    # Generate release date
    today = datetime.today().strftime('%Y%m%d')
    release_date = input(f"Enter the new release date (default: {today}): ") or today

    # Generate release version based on the new version
    release_version = extract_release_version(new_version)

    update_plugin_xml(plugin_xml_path, release_date, release_version, new_version)
    update_build_gradle(build_gradle_path, current_version, new_version)

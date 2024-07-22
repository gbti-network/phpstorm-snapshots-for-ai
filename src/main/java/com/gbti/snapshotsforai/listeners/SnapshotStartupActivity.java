package com.gbti.snapshotsforai;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SnapshotStartupActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        SnapshotService snapshotService = project.getService(SnapshotService.class);
        try {
            snapshotService.initializeSnapshotDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

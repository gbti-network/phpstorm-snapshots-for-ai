package com.gbti.snapshotsforai;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SnapshotProjectManagerListener implements ProjectManagerListener {
    @Override
    public void projectOpened(@NotNull Project project) {
        SnapshotService snapshotService = ServiceManager.getService(project, SnapshotService.class);
        try {
            snapshotService.initializeSnapshotDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

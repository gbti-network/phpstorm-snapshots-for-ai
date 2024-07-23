package com.gbti.snapshotsforai.listeners;

import com.gbti.snapshotsforai.services.SnapshotService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SnapshotStartupActivity implements StartupActivity {
    private static final Logger LOG = Logger.getInstance(SnapshotStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("SnapshotStartupActivity is running for project: " + project.getName());

        SnapshotService snapshotService = project.getService(SnapshotService.class);
        try {
            LOG.info("Initializing snapshot directory");
            snapshotService.initializeSnapshotDirectory();
            LOG.info("Snapshot directory initialized successfully");
        } catch (IOException e) {
            LOG.error("Error initializing snapshot directory", e);
        } catch (Exception e) {
            LOG.error("Unexpected error during snapshot initialization", e);
        }
    }
}
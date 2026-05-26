package com.imnotndesh.truehub.ui.services.apps.details.appdetails.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppInfoScreen
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun PreviewAppInfoScreen() {
    TrueHubAppTheme {
        val dummyConfig = Config.ClientConfig(
            serverUrl = "ws://dummy:8080/websocket",
            insecure = true,
            enableDebugLogging = false,
            enablePing = false
        )
        val dummyClient = TrueNASClient(dummyConfig)
        val dummyManager = TrueNASApiManager(
            client = dummyClient,
            applicationContext = LocalContext.current
        )

        AppInfoScreen(
            app = mockAppForPreview(),
            manager = dummyManager,
            onNavigateBack = {},
            onDeleteSuccess = {},
            onNavigateToMarketplaceCategory = {},
            onNavigateToMarketplaceAppDetails = {},
            onEditClick = {}
        )
    }
}

private fun mockAppForPreview(): Apps.AppQueryResponse {
    return Apps.AppQueryResponse(
        name = "plex",
        id = "plex-12345",
        state = "RUNNING",
        upgrade_available = true,
        latestVersion = "1.32.8.123",
        image_Updates_available = false,
        customApp = false,
        migrated = false,
        humanVersion = "1.32.8.123",
        version = "1.32.8.123",
        metadata = Apps.Metadata(
            title = "Plex Media Server",
            description = "<b>Stream movies, TV shows, and music.</b> Plex organizes your media and lets you access it from any device.",
            icon = "https://cdn.jsdelivr.net/gh/truenas/apps@master/charts/plex/icon.png",
            train = "stable",
            categories = listOf("Media", "Streaming", "Video"),
            keywords = listOf("plex", "media", "streaming", "movies", "tv shows"),
            screenshots = listOf(
                "https://example.com/screenshot1.png",
                "https://example.com/screenshot2.png"
            ),
            home = "https://plex.tv",
            sources = listOf("https://github.com/plexinc/pms-docker"),
            maintainers = listOf(Apps.Maintainer("Plex Inc", email = "support@plex.tv")),
            capabilities = listOf(
                Apps.Capability("Hardware Transcoding", "Use GPU for encoding/decoding"),
                Apps.Capability("DLNA", "Enable DLNA server")
            ),
            runAsContext = listOf(Apps.RunAsContext(uid = 1000, gid = 1000, userName = "plex")),
            hostMounts = listOf(Apps.HostMount("/mnt/pool/media", "Media library folder")),
            libVersion = "2.0.0",
            dateAdded = "2024-01-15",
            lastUpdate = "2025-02-20"
        ),
        versionInfo = Apps.VersionInfo(
            changelog = "- Fixed audio sync issues\n- Improved hardware transcoding performance\n- Updated underlying libraries",
            upgradeNotes = "⚠️ **Important:** Backup your database before upgrading. Some plugin APIs have changed."
        ),
        portals = mapOf("Web UI" to "http://192.168.1.100:32400/web"),
        activeWorkloads = Apps.ActiveWorkloads(
            containers = 1,
            usedPorts = listOf(
                Apps.UsedPort(
                    containerPort = 32400,
                    protocol = "tcp",
                    hostPorts = listOf(Apps.HostPort(hostPort = 32400, hostIp = "0.0.0.0"))
                )
            ),
            containerDetails = listOf(
                Apps.ContainerDetail(
                    id = "abc123def456",
                    serviceName = "plex",
                    image = "plexinc/pms-docker:1.32.8.123",
                    state = "running",
                    portConfig = listOf(
                        Apps.UsedPort(
                            containerPort = 32400,
                            protocol = "tcp",
                            hostPorts = listOf(Apps.HostPort(32400, "0.0.0.0"))
                        )
                    ),
                    volumeMounts = listOf(
                        Apps.Volume("/config", "/config", "rw", "volume"),
                        Apps.Volume("/data", "/media", "ro", "bind")
                    )
                )
            ),
            images = listOf("plexinc/pms-docker:1.32.8.123"),
            networks = listOf(
                Apps.Network(
                    name = "plex_bridge",
                    id = "net123",
                    driver = "bridge",
                    scope = "local",
                    enableIPv6 = false,
                    ipam = Apps.IPAM(
                        config = listOf(Apps.IPAMConfig("172.18.0.0/16", "172.18.0.1"))
                    )
                )
            ),
            volumes = listOf(
                Apps.Volume("/mnt/pool/apps/plex/config", "/config", "rw", "bind"),
                Apps.Volume("/mnt/pool/media", "/media", "ro", "bind")
            )
        ),
        notes = "To access the Plex Web UI, click the portal link above. For hardware transcoding, ensure your GPU is passed through.",
        migratedFromKubernetes = false
    )
}
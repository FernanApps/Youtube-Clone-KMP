package org.company.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import org.company.app.YoutubeWebView.YoutubeWebViewScreen
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.awt.Desktop
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.net.URI
import javax.swing.JOptionPane

internal actual fun openUrl(url: String?) {
    val uri = url?.let { URI.create(it) } ?: return
    Desktop.getDesktop().browse(uri)
}


@Composable
internal actual fun VideoPlayer(modifier: Modifier, url: String?, thumbnail: String?) {
    Napier.d { "Video Url ==== $url $thumbnail" }
    // https://www.youtube.com/watch?v=K_CbgLpvH9E to "https://www.youtube.com/embed/$videoKey"
    val videoKey = url!!.substringAfter("v=")
    val videoEmbedUrl = "https://www.youtube.com/embed/$videoKey"
    YoutubeWebViewScreen(modifier, videoEmbedUrl)
    // VideoPlayerFFMpeg(modifier = modifier, file = url.toString())
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal actual fun Notify(message: String) {
    if (SystemTray.isSupported()) {
        val tray = SystemTray.getSystemTray()
        val image = Toolkit.getDefaultToolkit().createImage("logo.webp")
        val trayIcon = TrayIcon(image, "Desktop Notification")
        tray.add(trayIcon)
        trayIcon.displayMessage("Desktop Notification", message, TrayIcon.MessageType.INFO)
    } else {
        // Fallback for systems that don't support SystemTray
        JOptionPane.showMessageDialog(
            null,
            message,
            "Desktop Notification",
            JOptionPane.INFORMATION_MESSAGE
        )
    }
}

@Composable
internal actual fun ShareManager(title: String, videoUrl: String) {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(videoUrl))
    }
}

@Composable
internal actual fun ShortsVideoPlayer(url: String?) {
    var isPlayed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow, contentDescription = "Play",
                modifier = Modifier.size(65.dp)
                    .clickable {
                        isPlayed = !isPlayed
                    },
                tint = Color.White
            )
            AnimatedVisibility(isPlayed) {
                VideoPlayerFFMpeg(modifier = Modifier.fillMaxWidth(), file = url.toString())
            }
        }
    }
}
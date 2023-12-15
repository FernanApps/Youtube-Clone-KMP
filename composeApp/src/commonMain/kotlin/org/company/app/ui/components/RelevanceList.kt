package org.company.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import org.company.app.data.model.videos.Youtube
import org.company.app.domain.usecases.YoutubeState

@Composable
fun RelevanceList(stateRelevance: YoutubeState) {
    var relevanceData by remember { mutableStateOf<Youtube?>(null) }

    when (stateRelevance) {
        is YoutubeState.LOADING -> {
            LoadingBox()
        }

        is YoutubeState.SUCCESS -> {
            var data = (stateRelevance as YoutubeState.SUCCESS).youtube
            relevanceData = data

        }

        is YoutubeState.ERROR -> {
            val error = (stateRelevance as YoutubeState.ERROR).error
             ErrorBox(error)
        }
    }
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            LazyVerticalGrid(columns = GridCells.Adaptive(300.dp)) {
                relevanceData?.let {
                    items(it.items) { videos ->
                        VideoItemCard(videos)
                    }
                }
            }
        }
    }
}
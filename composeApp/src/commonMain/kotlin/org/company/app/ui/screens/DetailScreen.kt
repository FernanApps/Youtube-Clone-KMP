package org.company.app.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.UnfoldMoreDouble
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.seiko.imageloader.rememberImagePainter
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.company.app.Notify
import org.company.app.ShareManager
import org.company.app.VideoPlayer
import org.company.app.data.model.channel.Channel
import org.company.app.data.model.comments.Comments
import org.company.app.data.model.videos.Item
import org.company.app.data.model.videos.Youtube
import org.company.app.domain.repository.Repository
import org.company.app.domain.usecases.ChannelState
import org.company.app.domain.usecases.CommentsState
import org.company.app.domain.usecases.YoutubeState
import org.company.app.presentation.MainViewModel
import org.company.app.theme.LocalThemeIsDark
import org.company.app.ui.components.CommentsList
import org.company.app.ui.components.ErrorBox
import org.company.app.ui.components.LoadingBox
import org.company.app.ui.components.RelevanceList
import org.company.app.ui.components.formatVideoDuration
import org.company.app.utils.Constant.VIDEO_URL

class DetailScreen(
    private val video: Item? = null,
    private val search: org.company.app.data.model.search.Item? = null
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val repository = remember { Repository() }
        val viewModel = remember { MainViewModel(repository) }
        var state by remember { mutableStateOf<ChannelState>(ChannelState.LOADING) }
        var stateRelevance by remember { mutableStateOf<YoutubeState>(YoutubeState.LOADING) }
        var channelData by remember { mutableStateOf<Channel?>(null) }
        var commentData by remember { mutableStateOf<Comments?>(null) }
        var videoDetail by remember { mutableStateOf<Youtube?>(null) }
        var descriptionEnabled by remember { mutableStateOf(false) }
        var displayVideoPlayer by remember { mutableStateOf(false) }
        var isCommentLive by remember { mutableStateOf(false) }
        var isShareEnabled by remember { mutableStateOf(false) }
        val navigator = LocalNavigator.current
        val isDark by LocalThemeIsDark.current

        LaunchedEffect(Unit) {
            //Channel Details
            if (video?.snippet?.channelId.isNullOrBlank()) {
                viewModel.getChannelDetails(search?.snippet?.channelId.toString())
            } else {
                viewModel.getChannelDetails(video?.snippet?.channelId.toString())
            }
            //Video Comments
            if (video?.id.isNullOrBlank()) {
                viewModel.getVideoComments(search?.id.toString(), order = "relevance")
            } else {
                viewModel.getVideoComments(video?.id.toString(), order = "relevance")
            }
            viewModel.getRelevance()
        }
        state = viewModel.channelDetails.collectAsState().value
        stateRelevance = viewModel.relevance.collectAsState().value
        val commentsState by viewModel.videoComments.collectAsState()

        when (state) {
            is ChannelState.LOADING -> {
                LoadingBox()
            }

            is ChannelState.SUCCESS -> {
                var data = (state as ChannelState.SUCCESS).channel
                channelData = data

            }

            is ChannelState.ERROR -> {
                val error = (state as ChannelState.ERROR).error
                ErrorBox(error)
            }
        }

        when (commentsState) {
            is CommentsState.LOADING -> {
                //LoadingBox()
            }

            is CommentsState.SUCCESS -> {
                val data = (commentsState as CommentsState.SUCCESS).comments
                commentData = data
            }

            is CommentsState.ERROR -> {
                val error = (commentsState as CommentsState.ERROR).error
                ErrorBox(error)
            }
        }

        //https://www.youtube.com/watch?v=${video.id}
        //"http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(state = rememberScrollState())
        ) {

            // Thumbnail
            if (displayVideoPlayer) {
                if (video?.id.isNullOrBlank()) {
                    VideoPlayer(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        url = "https://www.youtube.com/watch?v=${search?.id}",
                        thumbnail = search?.snippet?.thumbnails?.high?.url
                    )
                } else {
                    VideoPlayer(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        url = "https://www.youtube.com/watch?v=${video?.id}",
                        thumbnail = video?.snippet?.thumbnails?.high?.url
                    )
                }

            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {

                    val image: Resource<Painter> =
                        asyncPainterResource(data = if (video?.snippet?.thumbnails?.high?.url.isNullOrBlank()) search?.snippet?.thumbnails?.high?.url.toString() else video?.snippet?.thumbnails?.high?.url.toString())
                    KamelImage(
                        resource = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onLoading = {
                            CircularProgressIndicator(it)
                        },
                        onFailure = {
                            Text(text = "Failed to Load Image")
                        },
                        animationSpec = tween()
                    )

                    //play icon
                    Box(
                        modifier = Modifier.width(89.dp).height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                displayVideoPlayer = !displayVideoPlayer
                            }, modifier = Modifier.align(alignment = Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "play icon",
                                tint = Color.White,
                                modifier = Modifier.size(300.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { navigator?.pop() },
                        modifier = Modifier.padding(top = 8.dp, start = 6.dp)
                            .align(alignment = Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Arrow Back",
                            tint = Color.White
                        )
                    }

                    // Video Total Time
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.primary)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = video?.contentDetails?.duration?.let { formatVideoDuration(it) }
                                ?: "00:00",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Title and Arrow Down Icon
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (video?.snippet?.title.isNullOrBlank()) {

                    Text(
                        text = search?.snippet?.title.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(0.9f)
                    )

                } else {
                    Text(
                        text = video?.snippet?.title.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(0.9f)
                    )
                }
                Icon(imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).clickable {
                        descriptionEnabled = true
                    })
            }

            // Views, Date, Likes, Dislikes, Share, Download, Save
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val views = video?.statistics?.viewCount.toString()
                val pubDate =
                    if (video?.snippet?.publishedAt.isNullOrBlank()) search?.snippet?.publishedAt.toString() else video?.snippet?.publishedAt.toString()
                Text(
                    text = "${formatViewCount(views)} views - ${getFormattedDate(pubDate)}",
                    fontSize = 14.sp
                )
            }
            // Horizontal Divider
            Divider(
                modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 8.dp),
                thickness = 4.dp,
                color = Color.DarkGray
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                // Thumbs Up
                Card(
                    modifier = Modifier.height(40.dp).padding(4.dp).background(
                        color = Color.White, shape = RoundedCornerShape(8.dp)
                    ),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clickable { },
                            tint = Color.Black
                        )

                        Text(
                            text = formatLikes(video?.statistics?.likeCount),
                            fontSize = 14.sp,
                            color = if (isDark) Color.White else Color.Black
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Vertical line
                        Box(
                            modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.White)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Thumbs Down
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clickable { },
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                }

                // Share
                Card(
                    modifier = Modifier.height(40.dp).padding(4.dp),
                    onClick = {
                              isShareEnabled = !isShareEnabled
                    },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isDark) Color.White else Color.Black
                        )

                        Text(
                            text = "Share",
                            fontSize = 14.sp,
                            color = if (isDark) Color.White else Color.Black
                        )
                    }
                    if (isShareEnabled){
                        ShareManager(title = video?.snippet?.title.toString(), videoUrl =VIDEO_URL + video?.id)
                    }
                }

                // Download
                Card(
                    modifier = Modifier.height(40.dp).padding(4.dp),
                    onClick = { /* Handle Download click */ },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isDark) Color.White else Color.Black
                        )

                        Text(
                            text = "Download",
                            fontSize = 14.sp,
                            color = if (isDark) Color.White else Color.Black
                        )
                    }
                }

                // Save
                Card(
                    modifier = Modifier.height(40.dp).padding(4.dp),
                    onClick = { /* Handle Save click */ },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(6.dp))

            // Horizontal Divider
            Divider(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                thickness = 1.dp, // Adjust the thickness as needed
                color = Color.LightGray // Use a different color for better visibility
            )


            // Channel Section
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Channel Image
                channelData?.items?.get(0)?.snippet?.thumbnails?.default?.url?.let {
                    rememberImagePainter(
                        it
                    )
                }?.let {
                    Image(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(CircleShape).clickable {
                            navigator?.push(ChannelScreen(channelData!!.items[0]))
                        },
                        contentScale = ContentScale.FillBounds
                    )
                }

                // Channel Info
                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val channelTitle =
                        if (video?.snippet?.channelTitle.isNullOrBlank()) search?.snippet?.channelTitle.toString() else video?.snippet?.channelTitle.toString()

                    Text(
                        text = channelTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                    Text(
                        text = "${formatSubscribers(channelData?.items?.get(0)?.statistics?.subscriberCount)} Subscribers",
                        fontSize = 14.sp
                    )

                }
                Text(text = "SUBSCRIBE",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { /* Handle subscribe click */ })
            }

            // Horizontal Divider
            Divider(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                thickness = 1.dp, // Adjust the thickness as needed
                color = Color.LightGray // Use a different color for better visibility
            )


            // Sample Comment
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        isCommentLive = !isCommentLive
                    }
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Comments ${formatViewComments(video?.statistics?.commentCount.toString())}",
                        fontSize = MaterialTheme.typography.labelMedium.fontSize
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.UnfoldMoreDouble,
                        contentDescription = "More Comments Icon"
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    val commentProfile: Resource<Painter> =
                        asyncPainterResource(data = commentData?.items?.get(0)?.snippet?.channelId.toString())
                    val demoImage: Resource<Painter> =
                        asyncPainterResource(data = channelData?.items?.get(0)?.brandingSettings?.image?.bannerExternalUrl.toString())
                    KamelImage(
                        resource = demoImage,
                        contentDescription = "Comment User Profile",
                        modifier = Modifier.size(25.dp).clip(shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val firstComment =
                        commentData?.items?.get(0)?.snippet?.topLevelComment?.snippet?.textOriginal.toString()
                    Text(
                        text = firstComment,
                        modifier = Modifier.padding(start = 3.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                }
            }


            // Horizontal Divider
            Divider(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )
            // More Videos Section (Lazy Column)
            Text(
                text = "More Videos",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            RelevanceList(stateRelevance)

            if (descriptionEnabled) {
                ModalBottomSheet(
                    onDismissRequest = {
                        descriptionEnabled = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true,
                        confirmValueChange = { true }),
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color.Black,
                    tonalElevation = 8.dp,
                    scrimColor = Color.Transparent,
                    dragHandle = null,
                    windowInsets = BottomSheetDefaults.windowInsets,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .verticalScroll(state = rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Description",
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                modifier = Modifier.fillMaxWidth().weight(1f).padding(start = 4.dp)
                            )

                            IconButton(onClick = {
                                descriptionEnabled = false
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Divider(
                            modifier = Modifier.fillMaxWidth().padding(2.dp),
                            thickness = 2.dp,
                            color = DividerDefaults.color
                        )

                        val videoTitle =
                            if (video?.snippet?.title.isNullOrBlank()) search?.snippet?.title.toString() else video?.snippet?.title.toString()
                        Text(
                            text = videoTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            maxLines = 2,
                            textAlign = TextAlign.Justify,
                            overflow = TextOverflow.Ellipsis,
                        )


                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(start = 10.dp, end = 2.dp, top = 4.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Channel Image
                            channelData?.items?.get(0)?.snippet?.thumbnails?.default?.url?.let {
                                rememberImagePainter(
                                    it
                                )
                            }?.let {
                                Image(
                                    painter = it,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp).clip(CircleShape).clickable {
                                        navigator?.push(ChannelScreen(channelData!!.items[0]))
                                    },
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            channelData?.items?.get(0)?.snippet?.title?.let {
                                Text(
                                    text = it,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    textAlign = TextAlign.Justify,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                        }

                        //Video Details
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 20.dp, start = 60.dp, end = 60.dp, bottom = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = formatLikes(video?.statistics?.likeCount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                                )

                                Text(
                                    text = "Likes",
                                    fontWeight = FontWeight.Normal,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = formatViewCount(video?.statistics?.viewCount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                                )

                                Text(
                                    text = "Views",
                                    fontWeight = FontWeight.Normal,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val (formattedMonth, day, year) = getFormattedDateLikeMonthDay(video?.snippet?.publishedAt.toString())

                                Text(
                                    text = "$formattedMonth $day",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                                )

                                Text(
                                    text = "$year",
                                    fontWeight = FontWeight.Normal,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            thickness = 2.dp,
                            color = DividerDefaults.color
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            var desc_expanded by remember { mutableStateOf(false) }
                            val videoDescription =
                                if (video?.snippet?.description.isNullOrBlank()) search?.snippet?.description.toString() else video?.snippet?.description.toString()
                            Text(
                                text = videoDescription,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                                    .padding(top = 16.dp, start = 4.dp, end = 4.dp),
                                maxLines = if (desc_expanded) 40 else 9,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )

                            Text(
                                text = if (desc_expanded) "less" else "more",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                modifier = Modifier.clickable {
                                    desc_expanded = !desc_expanded
                                }.align(alignment = Alignment.Bottom)
                            )
                        }


                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 8.dp,
                            color = DividerDefaults.color
                        )

                        channelData?.items?.get(0)?.snippet?.title?.let {
                            Text(
                                text = "More From $it",
                                fontWeight = FontWeight.Normal,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                modifier = Modifier.fillMaxWidth().padding(
                                    horizontal = 16.dp, vertical = 8.dp
                                ), // Adjust padding as needed
                                maxLines = 1,
                                textAlign = TextAlign.Justify,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        // Channel Section
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Channel Image
                            channelData?.items?.get(0)?.snippet?.thumbnails?.default?.url?.let {
                                rememberImagePainter(
                                    it
                                )
                            }?.let {
                                Image(
                                    painter = it,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp).clip(CircleShape).clickable {
                                        navigator?.push(ChannelScreen(channelData!!.items[0]))
                                    },
                                    contentScale = ContentScale.FillBounds
                                )
                            }

                            // Channel Info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val channelTitle =
                                    if (video?.snippet?.channelTitle.isNullOrBlank()) search?.snippet?.channelTitle.toString() else video?.snippet?.channelTitle.toString()

                                Text(
                                    text = channelTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${formatSubscribers(channelData?.items?.get(0)?.statistics?.subscriberCount)} Subscribers",
                                    fontSize = 14.sp
                                )

                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(
                                horizontal = 8.dp, vertical = 4.dp
                            ), // Adjust padding as needed
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedCard(
                                onClick = {},
                                shape = CardDefaults.outlinedShape,
                                enabled = true,
                                border = BorderStroke(width = 1.dp, color = Color.Black),
                                modifier = Modifier.weight(1f).padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.VideoLibrary,
                                        contentDescription = "Videos",
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    Text(
                                        "VIDEOS",
                                        textAlign = TextAlign.Center,
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            OutlinedCard(
                                onClick = {
                                    channelData?.let { channel ->
                                        navigator?.push(ChannelDetail(channel.items[0]))
                                    }
                                },
                                shape = CardDefaults.outlinedShape,
                                enabled = true,
                                border = BorderStroke(width = 1.dp, color = Color.Black),
                                modifier = Modifier.weight(1f).padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBox,
                                        contentDescription = "About",
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    Text(
                                        "ABOUT",
                                        textAlign = TextAlign.Center,
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
        if (isCommentLive) {
            if (video?.statistics?.commentCount.isNullOrBlank()) {
                Notify(message = "No Comments Found...")
            }
            var commentInput by remember { mutableStateOf("") }

            ModalBottomSheet(
                onDismissRequest = {
                    isCommentLive = false
                },
                sheetState = rememberModalBottomSheetState(),
                tonalElevation = ModalBottomSheetDefaults.Elevation,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Comment Title Section
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Comments",
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            isCommentLive = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Icon"
                            )
                        }
                    }
                    Divider(modifier = Modifier.fillMaxWidth(), color = DividerDefaults.color)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Top and Newest Icon Button
                    val buttons = listOf(
                        "Top",
                        "Newest"
                    )
                    var selectedButton by remember { mutableStateOf(buttons.first()) }

                    // Function to fetch comments based on the selected order
                    fun fetchComments(order: String) {
                        if (video?.id.isNullOrBlank()) {
                            viewModel.getVideoComments(search?.id.toString(), order)
                        } else {
                            viewModel.getVideoComments(video?.id.toString(), order)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        buttons.forEach { title ->
                            TextButton(
                                onClick = {
                                    selectedButton = title
                                    fetchComments(if (title == "Top") "relevance" else "time")
                                },
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(12.dp))
                                    .background(
                                        color = if (selectedButton == title) Color.Black else Color.Transparent
                                    )
                            ) {
                                Text(
                                    text = title,
                                    color = if (selectedButton == title) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }
                    }


                    // Terms and conditions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .background(color = Color.LightGray),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Remember to keep comments respectful and to follow our Community Guidelines",
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth().padding(12.dp)
                        )
                    }

                    Divider(modifier = Modifier.fillMaxWidth(), color = DividerDefaults.color)

                    // Comments Lists
                    commentData?.let { comments ->
                        CommentsList(comments, modifier = Modifier.weight(1f))
                    }

                    // Own Channel Image and TextField
                    Row(
                        modifier = Modifier
                            .background(color = Color.White)
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Own Channel Image
                        val ownChannelImage: Resource<Painter> =
                            asyncPainterResource(data = channelData?.items?.get(0)?.brandingSettings?.image?.bannerExternalUrl.toString())
                        KamelImage(
                            resource = ownChannelImage,
                            contentDescription = "Channel Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(25.dp)
                                .clip(CircleShape)
                                .align(alignment = Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // TextField
                        TextField(
                            value = commentInput,
                            onValueChange = {
                                commentInput = it
                            },
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(
                                    start = 8.dp,
                                    bottom = 8.dp
                                ),
                            placeholder = {
                                Text(text = "Add a comment...")
                            },
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                }
            }
        }
    }
}


// Utility functions for formatting
fun formatViewCount(count: String?): String {
    return count?.toDoubleOrNull()?.let {
        when {
            it >= 1_000_000_000 -> "${(it / 1_000_000_000).toInt()}B"
            it >= 1_000_000 -> "${(it / 1_000_000).toInt()}M"
            it >= 1_000 -> "${(it / 1_000).toInt()}K"
            else -> "$it"
        }
    } ?: "0"
}

fun formatViewComments(count: String?): String {
    return count?.toDoubleOrNull()?.let {
        when {
            it >= 1_000_000_000 -> "${(it / 1_000_000_000).toInt()}B"
            it >= 1_000_000 -> "${(it / 1_000_000).toInt()}M"
            it >= 1_000 -> "${(it / 1_000).toInt()}K"
            else -> "$it"
        }
    } ?: "0"
}

fun formatLikes(count: String?): String {
    return count?.toDoubleOrNull()?.let {
        when {
            it >= 1_000_000 -> "${(it / 1_000_000).toInt()}M"
            it >= 1_000 -> "${(it / 1_000).toInt()}K"
            else -> "$it"
        }
    } ?: "0"
}

fun formatSubscribers(count: String?): String {
    return count?.toDoubleOrNull()?.let {
        when {
            it >= 1_000_000_000 -> "${(it / 1_000_000_000).toInt()}B"
            it >= 1_000_000 -> "${(it / 1_000_000).toInt()}M"
            it >= 1_000 -> "${(it / 1_000).toInt()}K"
            else -> "$it"
        }
    } ?: "0"
}

fun getFormattedDate(publishedAt: String): String {
    return try {
        val instant = Instant.parse(publishedAt)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val currentInstant = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())

        val seconds = currentInstant.epochSeconds - instant.epochSeconds
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> "$seconds seconds ago"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            else -> "$days days ago"
        }
    } catch (e: Throwable) {
        "Unknown date"
    }
}

fun getFormattedDateLikeMonthDay(videoPublishedAt: String): Triple<String, Int, Int> {
    return try {
        val instant = Instant.parse(videoPublishedAt)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        val formattedMonth = months[localDateTime.monthNumber - 1]
        val dayOfMonth = localDateTime.dayOfMonth
        val year = localDateTime.year

        Triple(formattedMonth, dayOfMonth, year)
    } catch (e: Throwable) {
        Triple("Unknown", 0, 0)
    }
}


package com.bioverity.attendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bioverity.attendance.viewmodel.NotificationViewModel

private val Navy = Color(0xFF173B63)
private val Background = Color(0xFFF6F8FB)
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val UnreadBackground = Color(0xFFEFF5FC)


data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val time: String,
    val isRead: Boolean
)


enum class NotificationType {
    ATTENDANCE_SUCCESS,
    CHECK_IN_REMINDER,
    CHECK_OUT_REMINDER,
    LOCATION_WARNING,
    SYSTEM
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    personId: String,
    viewModel: NotificationViewModel,
    onBack: () -> Unit
) {

    val notifications by
    viewModel.notifications.collectAsState()

    val isLoading by
    viewModel.isLoading.collectAsState()

    val error by
    viewModel.error.collectAsState()


    LaunchedEffect(personId) {

        if (personId.isNotBlank()) {

            viewModel.loadNotifications(
                personId
            )
        }
    }


    Scaffold(

        containerColor = Background,

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,

                            contentDescription = "Back",

                            tint = Navy
                        )
                    }
                },

                actions = {

                    if (
                        notifications.any {
                            !it.isRead
                        }
                    ) {

                        Text(
                            text = "Mark all read",

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color = Navy,

                            modifier =
                                Modifier
                                    .clickable {

                                        viewModel.markAllAsRead(
                                            personId
                                        )
                                    }
                                    .padding(
                                        horizontal = 12.dp
                                    )
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Background
                    )
            )
        }
    ) { paddingValues ->

        when {

            isLoading -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color = Navy
                    )
                }
            }


            error != null &&
                    notifications.isEmpty() -> {

                NotificationErrorState(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),

                    message =
                        error ?: "Unable to load notifications"
                )
            }


            notifications.isEmpty() -> {

                EmptyNotifications(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                )
            }


            else -> {

                LazyColumn(

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),

                    contentPadding =
                        PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = notifications,
                        key = {
                            it.id
                        }
                    ) { notification ->

                        NotificationCard(

                            notification =
                                notification,

                            onClick = {

                                if (!notification.isRead) {

                                    viewModel.markAsRead(
                                        notification.id,
                                        personId
                                    )
                                }
                            },

                            onDelete = {

                                viewModel.deleteNotification(
                                    notification.id
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    val cardColor =
        if (notification.isRead) {
            Color.White
        } else {
            UnreadBackground
        }


    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(14.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = cardColor
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            verticalAlignment =
                Alignment.Top
        ) {

            NotificationIcon(
                type = notification.type
            )

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = notification.title,

                        fontSize = 15.sp,

                        fontWeight =
                            FontWeight.SemiBold,

                        color = TextPrimary,

                        modifier =
                            Modifier.weight(1f)
                    )

                    if (!notification.isRead) {

                        Box(
                            modifier =
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Navy)
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text = notification.message,

                    fontSize = 13.sp,

                    lineHeight = 19.sp,

                    color = TextSecondary
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Schedule,

                        contentDescription = null,

                        modifier =
                            Modifier.size(14.dp),

                        tint = TextSecondary
                    )

                    Spacer(
                        modifier =
                            Modifier.size(4.dp)
                    )

                    Text(
                        text = notification.time,

                        fontSize = 11.sp,

                        color = TextSecondary,

                        modifier =
                            Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDelete,

                        modifier =
                            Modifier.size(32.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Delete,

                            contentDescription =
                                "Delete notification",

                            modifier =
                                Modifier.size(18.dp),

                            tint = TextSecondary
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun NotificationIcon(
    type: NotificationType
) {

    val icon =
        when (type) {

            NotificationType.ATTENDANCE_SUCCESS ->
                Icons.Default.CheckCircle

            NotificationType.CHECK_IN_REMINDER ->
                Icons.Default.NotificationsNone

            NotificationType.CHECK_OUT_REMINDER ->
                Icons.Default.Schedule

            NotificationType.LOCATION_WARNING ->
                Icons.Default.Warning

            NotificationType.SYSTEM ->
                Icons.Default.NotificationsNone
        }


    Box(

        modifier =
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White),

        contentAlignment =
            Alignment.Center
    ) {

        Icon(
            imageVector = icon,

            contentDescription = null,

            modifier =
                Modifier.size(22.dp),

            tint = Navy
        )
    }
}


@Composable
private fun EmptyNotifications(
    modifier: Modifier = Modifier
) {

    Column(

        modifier =
            modifier.padding(32.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Box(

            modifier =
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(UnreadBackground),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.NotificationsNone,

                contentDescription = null,

                modifier =
                    Modifier.size(36.dp),

                tint = Navy
            )
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Text(
            text = "No notifications",

            fontSize = 18.sp,

            fontWeight = FontWeight.Bold,

            color = TextPrimary
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Text(
            text = "You're all caught up.",

            fontSize = 14.sp,

            color = TextSecondary
        )
    }
}


@Composable
private fun NotificationErrorState(
    modifier: Modifier,
    message: String
) {

    Column(

        modifier =
            modifier.padding(32.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Icon(
            imageVector =
                Icons.Default.Warning,

            contentDescription = null,

            modifier =
                Modifier.size(48.dp),

            tint = Navy
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(
            text = "Unable to load notifications",

            fontSize = 18.sp,

            fontWeight = FontWeight.Bold,

            color = TextPrimary
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text = message,

            fontSize = 13.sp,

            color = TextSecondary
        )
    }
}
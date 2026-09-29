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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bioverity.attendance.viewmodel.NotificationViewModel

private val Navy = Color(0xFF173B63)
private val NavyDark = Color(0xFF102F50)

private val Background = Color(0xFFF6F8FB)
private val Surface = Color.White

private val TextPrimary = Color(0xFF172033)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor = Color(0xFFE5E7EB)

private val UnreadBackground = Color(0xFFF0F6FD)
private val UnreadBadgeBackground = Color(0xFFE3EEF9)

private val SuccessColor = Color(0xFF198754)
private val SuccessBackground = Color(0xFFEAF7F0)

private val WarningColor = Color(0xFFB7791F)
private val WarningBackground = Color(0xFFFFF7E6)

private val LocationColor = Color(0xFF7C3AED)
private val LocationBackground = Color(0xFFF3EEFF)

private val SystemColor = Color(0xFF2563EB)
private val SystemBackground = Color(0xFFEDF4FF)

private val ErrorColor = Color(0xFFDC2626)
private val ErrorBackground = Color(0xFFFFEEEE)


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

    LOCATION_VERIFICATION_FAILED,
    SYSTEM_NOTIFICATION
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

    val unreadCount =
        notifications.count {
            !it.isRead
        }


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

                    Column {

                        Text(
                            text = "Notifications",

                            fontSize = 20.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color = NavyDark
                        )

                        if (unreadCount > 0) {

                            Text(
                                text =
                                    "$unreadCount unread",

                                fontSize = 12.sp,

                                fontWeight =
                                    FontWeight.Medium,

                                color = TextSecondary
                            )
                        }
                    }
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,

                            contentDescription =
                                "Back",

                            tint = Navy
                        )
                    }
                },

                actions = {

                    if (unreadCount > 0) {

                        Text(
                            text = "Mark all read",

                            fontSize = 12.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color = Navy,

                            modifier =
                                Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            8.dp
                                        )
                                    )
                                    .clickable {

                                        viewModel.markAllAsRead(
                                            personId
                                        )
                                    }
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 8.dp
                                    )
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            Background
                    )
            )
        }

    ) { paddingValues ->


        when {

            isLoading -> {

                NotificationLoadingState(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                paddingValues
                            )
                )
            }


            error != null &&
                    notifications.isEmpty() -> {

                NotificationErrorState(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                paddingValues
                            ),

                    message =
                        error
                            ?: "Unable to load notifications"
                )
            }


            notifications.isEmpty() -> {

                EmptyNotifications(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                paddingValues
                            )
                )
            }


            else -> {

                LazyColumn(

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                paddingValues
                            ),

                    contentPadding =
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 14.dp,
                            bottom = 28.dp
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

                                if (
                                    !notification.isRead
                                ) {

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

    val style =
        notificationStyle(
            notification.type
        )

    val cardColor =
        if (notification.isRead) {
            Surface
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
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    cardColor
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(15.dp),

            verticalAlignment =
                Alignment.Top
        ) {

            NotificationIcon(
                style = style
            )


            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.Top
                ) {

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text =
                                    notification.title,

                                fontSize = 15.sp,

                                fontWeight =
                                    FontWeight.SemiBold,

                                color =
                                    TextPrimary
                            )

                            if (
                                !notification.isRead
                            ) {

                                Spacer(
                                    modifier =
                                        Modifier.width(7.dp)
                                )

                                Box(

                                    modifier =
                                        Modifier
                                            .size(7.dp)
                                            .clip(
                                                CircleShape
                                            )
                                            .background(
                                                Navy
                                            )
                                )
                            }
                        }


                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )


                        Text(
                            text =
                                notification.message,

                            fontSize = 13.sp,

                            lineHeight = 19.sp,

                            color =
                                TextSecondary
                        )
                    }


                    IconButton(

                        onClick =
                            onDelete,

                        modifier =
                            Modifier.size(32.dp)
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.DeleteOutline,

                            contentDescription =
                                "Delete notification",

                            modifier =
                                Modifier.size(19.dp),

                            tint =
                                Color(0xFF9CA3AF)
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )


                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(

                        modifier =
                            Modifier
                                .clip(
                                    RoundedCornerShape(
                                        6.dp
                                    )
                                )
                                .background(
                                    style.badgeBackground
                                )
                                .padding(
                                    horizontal = 7.dp,
                                    vertical = 4.dp
                                )
                    ) {

                        Text(

                            text =
                                style.label,

                            fontSize = 10.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                style.iconColor
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    Icon(

                        imageVector =
                            Icons.Default.Schedule,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(13.dp),

                        tint =
                            TextSecondary
                    )


                    Spacer(
                        modifier =
                            Modifier.width(4.dp)
                    )


                    Text(

                        text =
                            notification.time,

                        fontSize = 11.sp,

                        color =
                            TextSecondary
                    )
                }
            }
        }
    }
}


private data class NotificationStyle(

    val icon: androidx.compose.ui.graphics.vector.ImageVector,

    val iconColor: Color,

    val iconBackground: Color,

    val badgeBackground: Color,

    val label: String
)


private fun notificationStyle(
    type: NotificationType
): NotificationStyle {

    return when (type) {

        NotificationType.ATTENDANCE_SUCCESS ->

            NotificationStyle(

                icon =
                    Icons.Default.CheckCircle,

                iconColor =
                    SuccessColor,

                iconBackground =
                    SuccessBackground,

                badgeBackground =
                    SuccessBackground,

                label =
                    "Attendance"
            )


        NotificationType.CHECK_IN_REMINDER ->

            NotificationStyle(

                icon =
                    Icons.Default.NotificationsNone,

                iconColor =
                    WarningColor,

                iconBackground =
                    WarningBackground,

                badgeBackground =
                    WarningBackground,

                label =
                    "Reminder"
            )


        NotificationType.CHECK_OUT_REMINDER ->

            NotificationStyle(

                icon =
                    Icons.Default.Schedule,

                iconColor =
                    WarningColor,

                iconBackground =
                    WarningBackground,

                badgeBackground =
                    WarningBackground,

                label =
                    "Reminder"
            )


        NotificationType.LOCATION_VERIFICATION_FAILED ->

            NotificationStyle(

                icon =
                    Icons.Default.LocationOn,

                iconColor =
                    LocationColor,

                iconBackground =
                    LocationBackground,

                badgeBackground =
                    LocationBackground,

                label =
                    "Location"
            )


        NotificationType.SYSTEM_NOTIFICATION ->

            NotificationStyle(

                icon =
                    Icons.Default.SystemUpdate,

                iconColor =
                    SystemColor,

                iconBackground =
                    SystemBackground,

                badgeBackground =
                    SystemBackground,

                label =
                    "System"
            )
    }
}


@Composable
private fun NotificationIcon(
    style: NotificationStyle
) {

    Box(

        modifier =
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    style.iconBackground
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Icon(

            imageVector =
                style.icon,

            contentDescription =
                null,

            modifier =
                Modifier.size(22.dp),

            tint =
                style.iconColor
        )
    }
}


@Composable
private fun NotificationLoadingState(
    modifier: Modifier
) {

    Column(

        modifier =
            modifier.padding(32.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        CircularProgressIndicator(
            modifier =
                Modifier.size(34.dp),

            strokeWidth = 3.dp,

            color = Navy
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(

            text =
                "Loading notifications...",

            fontSize = 14.sp,

            color =
                TextSecondary
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
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        UnreadBackground
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(

                imageVector =
                    Icons.Default.NotificationsNone,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(38.dp),

                tint =
                    Navy
            )
        }


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        Text(

            text =
                "You're all caught up",

            fontSize = 19.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                TextPrimary
        )


        Spacer(
            modifier =
                Modifier.height(7.dp)
        )


        Text(

            text =
                "You don't have any new notifications.",

            fontSize = 14.sp,

            color =
                TextSecondary
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

        Box(

            modifier =
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        ErrorBackground
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(

                imageVector =
                    Icons.Default.ErrorOutline,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(36.dp),

                tint =
                    ErrorColor
            )
        }


        Spacer(
            modifier =
                Modifier.height(18.dp)
        )


        Text(

            text =
                "Unable to load notifications",

            fontSize = 18.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                TextPrimary
        )


        Spacer(
            modifier =
                Modifier.height(7.dp)
        )


        Text(

            text =
                message,

            fontSize = 13.sp,

            color =
                TextSecondary
        )
    }
}
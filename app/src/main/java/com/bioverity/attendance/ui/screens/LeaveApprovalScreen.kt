
package com.bioverity.attendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bioverity.attendance.data.model.LeaveApplication
import com.bioverity.attendance.viewmodel.LeaveViewModel
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveApprovalScreen(
    viewModel: LeaveViewModel,
    onBack: () -> Unit
) {
    val leaves by viewModel.allLeaveApplications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isProcessing by viewModel.isProcessingApproval.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllLeaveApplications()
    }

    val pendingLeaves =
        leaves.filter {
            it.status.equals("PENDING", ignoreCase = true)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Leave Approval",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${pendingLeaves.size} pending request(s)",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.refreshAllLeaves()
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                        MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when {

                isLoading && leaves.isEmpty() -> {

                    CircularProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }

                leaves.isEmpty() -> {

                    EmptyLeaveState(
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            16.dp
                        ),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        if (!errorMessage.isNullOrBlank()) {

                            item {

                                MessageCard(
                                    message =
                                        errorMessage
                                            ?: "",
                                    isError = true
                                )
                            }
                        }

                        if (!successMessage.isNullOrBlank()) {

                            item {

                                MessageCard(
                                    message =
                                        successMessage
                                            ?: "",
                                    isError = false
                                )
                            }
                        }

                        items(
                            items = leaves,
                            key = { it.id }
                        ) { leave ->

                            LeaveApprovalCard(
                                leave = leave,
                                isProcessing = isProcessing,
                                onApprove = {
                                    viewModel.approveLeave(
                                        leave.id
                                    )
                                },
                                onReject = {
                                    viewModel.rejectLeave(
                                        leave.id
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun LeaveApprovalCard(
    leave: LeaveApplication,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {

    val isPending =
        leave.status.equals(
            "PENDING",
            ignoreCase = true
        )

    val statusText =
        leave.status.uppercase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Leave Request",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text = "Employee ID: ${leave.personId}",
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }

                StatusBadge(
                    status = statusText
                )
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            DetailRow(
                label = "Leave date",
                value = leave.leaveDate
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            DetailRow(
                label = "Reason",
                value = leave.reason
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            DetailRow(
                label = "Submitted",
                value =
                    formatSriLankaDateTime(
                        leave.createdAt
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            DetailRow(
                label = "Last updated",
                value =
                    formatSriLankaDateTime(
                        leave.updatedAt
                    )
            )

            if (!leave.attachmentUrl.isNullOrBlank()) {

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Text(
                    text = "Attachment available",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    fontWeight =
                        FontWeight.Medium
                )
            }

            if (isPending) {

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Button(
                        onClick = onReject,
                        enabled = !isProcessing,
                        modifier =
                            Modifier.weight(1f),
                        shape =
                            RoundedCornerShape(12.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Close,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Text("Reject")
                    }

                    Spacer(
                        modifier =
                            Modifier.width(10.dp)
                    )

                    Button(
                        onClick = onApprove,
                        enabled = !isProcessing,
                        modifier =
                            Modifier.weight(1f),
                        shape =
                            RoundedCornerShape(12.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Check,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Text("Approve")
                    }
                }
            }
        }
    }
}


@Composable
private fun StatusBadge(
    status: String
) {

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {

        Text(
            text = status,
            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),
            style =
                MaterialTheme.typography.labelMedium,
            fontWeight =
                FontWeight.Bold
        )
    }
}


@Composable
private fun DetailRow(
    label: String,
    value: String
) {

    Column {

        Text(
            text = label,
            style =
                MaterialTheme.typography.labelMedium,
            fontWeight =
                FontWeight.SemiBold
        )

        Spacer(
            modifier =
                Modifier.height(2.dp)
        )

        Text(
            text = value.ifBlank { "-" },
            style =
                MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun MessageCard(
    message: String,
    isError: Boolean
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant
            )
    ) {

        Text(
            text = message,
            modifier =
                Modifier.padding(14.dp),
            style =
                MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun EmptyLeaveState(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .padding(32.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "No leave requests",
            style =
                MaterialTheme.typography.titleMedium,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Text(
            text =
                "There are currently no leave applications to review.",
            style =
                MaterialTheme.typography.bodyMedium
        )
    }
}


private fun formatSriLankaDateTime(
    value: String?
): String {

    if (value.isNullOrBlank()) {
        return "-"
    }

    return try {

        val sriLankaZone =
            ZoneId.of("Asia/Colombo")

        val formatter =
            DateTimeFormatter.ofPattern(
                "dd MMM yyyy, hh:mm a",
                Locale.ENGLISH
            )

        OffsetDateTime
            .parse(value)
            .atZoneSameInstant(
                sriLankaZone
            )
            .format(formatter)

    } catch (_: Exception) {

        "-"
    }
}


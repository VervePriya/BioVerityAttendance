
package com.bioverity.attendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import coil.compose.AsyncImage

import com.bioverity.attendance.data.model.Employee
import com.bioverity.attendance.data.session.AppSession
import com.bioverity.attendance.viewmodel.ProfileViewModel


@Composable
fun ProfileScreen(
    personId: String?,
    onNavigate: (Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {

    // ============================================================
    // CONTEXT
    // ============================================================

    val context =
        androidx.compose.ui.platform.LocalContext.current


    // ============================================================
    // SESSION PERSON ID
    // ============================================================

    val sessionPersonId =
        AppSession.getPersonId(context)


    // ============================================================
    // ACTUAL PERSON ID
    // ============================================================

    val actualPersonId =
        personId ?: sessionPersonId


    // ============================================================
    // LOGOUT DIALOG STATE
    // ============================================================

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }


    // ============================================================
    // LOAD EMPLOYEE
    // ============================================================

    LaunchedEffect(actualPersonId) {

        if (!actualPersonId.isNullOrBlank()) {

            viewModel.loadEmployee(
                actualPersonId
            )
        }
    }


    // ============================================================
    // EMPLOYEE
    // ============================================================

    val employee =
        viewModel.employee


    // ============================================================
    // PROFILE SCREEN
    // ============================================================

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
    ) {

        // ========================================================
        // TOP BAR
        // ========================================================

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(

                onClick = {
                    onNavigate(0)
                }
            ) {

                Icon(

                    imageVector =
                        Icons.Default.ArrowBack,

                    contentDescription =
                        "Back"
                )
            }


            Spacer(
                modifier =
                    Modifier.width(4.dp)
            )


            Column {

                Text(

                    text = "Profile",

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Bold
                )


                Text(

                    text =
                        "Employee information",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }


        // ========================================================
        // LOADING
        // ========================================================

        if (
            viewModel.isLoading &&
            employee == null
        ) {

            ProfileLoadingState()

            return@Column
        }


        // ========================================================
        // ERROR
        // ========================================================

        if (
            viewModel.errorMessage != null &&
            employee == null
        ) {

            EmptyProfileState(

                message =
                    viewModel.errorMessage
                        ?: "Unable to load employee information",

                onNavigate =
                    onNavigate
            )

            return@Column
        }


        // ========================================================
        // NO DATA
        // ========================================================

        if (employee == null) {

            EmptyProfileState(

                message =
                    "Employee information not found",

                onNavigate =
                    onNavigate
            )

            return@Column
        }


        // ========================================================
        // PROFILE CONTENT
        // ========================================================

        ProfileContent(

            employee =
                employee,

            onLogout = {

                showLogoutDialog = true
            }
        )
    }


    // ============================================================
    // LOGOUT CONFIRMATION DIALOG
    // ============================================================

    if (showLogoutDialog) {

        AlertDialog(

            onDismissRequest = {

                showLogoutDialog = false
            },


            // ----------------------------------------------------
            // TITLE
            // ----------------------------------------------------

            title = {

                Text(

                    text =
                        "Logout",

                    fontWeight =
                        FontWeight.Bold
                )
            },


            // ----------------------------------------------------
            // MESSAGE
            // ----------------------------------------------------

            text = {

                Text(
                    text =
                        "Are you sure you want to logout?"
                )
            },


            // ----------------------------------------------------
            // CONFIRM
            // ----------------------------------------------------

            confirmButton = {

                TextButton(

                    onClick = {

                        /*
                         * Close the dialog first.
                         */
                        showLogoutDialog = false


                        /*
                         * AppNavigation handles:
                         *
                         * AppSession.logout()
                         * isLoggedIn = false
                         * returning to LoginScreen
                         */
                        onLogout()
                    }
                ) {

                    Text(

                        text =
                            "Logout",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },


            // ----------------------------------------------------
            // CANCEL
            // ----------------------------------------------------

            dismissButton = {

                TextButton(

                    onClick = {

                        showLogoutDialog = false
                    }
                ) {

                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }
}


// ====================================================================
// PROFILE CONTENT
// ====================================================================

@Composable
private fun ProfileContent(
    employee: Employee,
    onLogout: () -> Unit
) {

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 20.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        // ============================================================
        // PROFILE HEADER
        // ============================================================

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(24.dp),

            colors =
                CardDefaults.cardColors(

                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surface
                ),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 24.dp
                        ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                ProfilePhoto(

                    name =
                        employee.name,

                    imageUrl =
                        employee.imageUrl
                )


                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )


                Text(

                    text =
                        employee.name,

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,

                    fontWeight =
                        FontWeight.Bold,

                    maxLines = 1,

                    overflow =
                        TextOverflow.Ellipsis
                )


                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )


                Text(

                    text =
                        employee.position
                            ?: "Employee",

                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                if (
                    !employee.employeeId
                        .isNullOrBlank()
                ) {

                    Surface(

                        shape =
                            RoundedCornerShape(50.dp),

                        color =
                            MaterialTheme
                                .colorScheme
                                .primaryContainer
                    ) {

                        Row(

                            modifier =
                                Modifier.padding(
                                    horizontal = 14.dp,
                                    vertical = 7.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Business,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(16.dp),

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )


                            Text(

                                text =
                                    employee.employeeId,

                                style =
                                    MaterialTheme
                                        .typography
                                        .labelLarge,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .primary,

                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // ============================================================
        // PERSONAL INFORMATION
        // ============================================================

        ProfileSectionTitle(
            title = "Personal Information"
        )


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        ProfileInformationCard {

            ProfileInfoRow(

                icon =
                    Icons.Default.Person,

                label =
                    "Employee ID",

                value =
                    employee.employeeId
                        ?: "Not available"
            )


            ProfileDivider()


            ProfileInfoRow(

                icon =
                    Icons.Default.Email,

                label =
                    "Email",

                value =
                    employee.email
                        ?: "Not available"
            )


            ProfileDivider()


            ProfileInfoRow(

                icon =
                    Icons.Default.Phone,

                label =
                    "Phone",

                value =
                    employee.phone
                        ?: "Not available"
            )
        }


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // ============================================================
        // WORK INFORMATION
        // ============================================================

        ProfileSectionTitle(
            title = "Work Information"
        )


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        ProfileInformationCard {

            ProfileInfoRow(

                icon =
                    Icons.Default.Work,

                label =
                    "Position",

                value =
                    employee.position
                        ?: "Not available"
            )


            ProfileDivider()


            ProfileInfoRow(

                icon =
                    Icons.Default.Business,

                label =
                    "Department",

                value =
                    employee.department
                        ?: "Not available"
            )
        }


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        // ============================================================
        // LOGOUT BUTTON
        // ============================================================

        TextButton(

            onClick = onLogout,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Icon(

                imageVector =
                    Icons.Default.Logout,

                contentDescription =
                    "Logout",

                tint =
                    MaterialTheme
                        .colorScheme
                        .error
            )


            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )


            Text(

                text =
                    "Logout",

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                fontWeight =
                    FontWeight.SemiBold
            )
        }


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )
    }
}


// ====================================================================
// PROFILE PHOTO
// ====================================================================

@Composable
private fun ProfilePhoto(
    name: String,
    imageUrl: String?
) {

    Box(

        modifier =
            Modifier
                .size(118.dp)
                .border(

                    width = 3.dp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                            .copy(alpha = 0.18f),

                    shape =
                        CircleShape
                )
                .padding(5.dp)
                .clip(CircleShape)
                .background(

                    MaterialTheme
                        .colorScheme
                        .primaryContainer
                ),

        contentAlignment =
            Alignment.Center
    ) {

        if (!imageUrl.isNullOrBlank()) {

            AsyncImage(

                model =
                    imageUrl,

                contentDescription =
                    "$name profile photo",

                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
            )

        } else {

            Icon(

                imageVector =
                    Icons.Default.Person,

                contentDescription =
                    "$name profile photo",

                modifier =
                    Modifier.size(58.dp),

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}


// ====================================================================
// INFORMATION CARD
// ====================================================================

@Composable
private fun ProfileInformationCard(
    content:
    @Composable
    androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Column(

            modifier =
                Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 6.dp
                ),

            content =
                content
        )
    }
}


// ====================================================================
// SECTION TITLE
// ====================================================================

@Composable
private fun ProfileSectionTitle(
    title: String
) {

    Text(

        text =
            title,

        modifier =
            Modifier.fillMaxWidth(),

        style =
            MaterialTheme
                .typography
                .titleMedium,

        fontWeight =
            FontWeight.Bold,

        color =
            MaterialTheme
                .colorScheme
                .onBackground
    )
}


// ====================================================================
// INFORMATION ROW
// ====================================================================

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 13.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(

            modifier =
                Modifier
                    .size(40.dp)
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .background(

                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(

                imageVector =
                    icon,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(20.dp),

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }


        Spacer(
            modifier =
                Modifier.width(14.dp)
        )


        Column(

            modifier =
                Modifier.weight(1f)
        ) {

            Text(

                text =
                    label,

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )


            Text(

                text =
                    value,

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,

                fontWeight =
                    FontWeight.Medium,

                maxLines = 2,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}


// ====================================================================
// DIVIDER
// ====================================================================

@Composable
private fun ProfileDivider() {

    Spacer(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(

                    MaterialTheme
                        .colorScheme
                        .outlineVariant
                        .copy(alpha = 0.5f)
                )
    )
}


// ====================================================================
// LOADING
// ====================================================================

@Composable
private fun ProfileLoadingState() {

    Box(

        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {

        Column(

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator()


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Text(

                text =
                    "Loading employee information...",

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                fontSize = 14.sp
            )
        }
    }
}


// ====================================================================
// EMPTY / ERROR
// ====================================================================

@Composable
private fun EmptyProfileState(
    message: String,
    onNavigate: (Int) -> Unit
) {

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Icon(

            imageVector =
                Icons.Default.Person,

            contentDescription =
                null,

            modifier =
                Modifier.size(50.dp),

            tint =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        Text(

            text =
                message,

            style =
                MaterialTheme
                    .typography
                    .bodyLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        IconButton(

            onClick = {
                onNavigate(0)
            }
        ) {

            Icon(

                imageVector =
                    Icons.Default.ArrowBack,

                contentDescription =
                    "Back to Home"
            )
        }
    }
}


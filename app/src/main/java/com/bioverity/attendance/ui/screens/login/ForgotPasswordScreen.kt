package com.bioverity.attendance.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel =
        viewModel()
) {

    val uiState by
    viewModel.uiState.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF7F8FA)
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            // -----------------------------------------------------
            // BACK BUTTON
            // -----------------------------------------------------

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                IconButton(
                    onClick = onBack,
                    enabled = !uiState.isLoading
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.ArrowBack,
                        contentDescription =
                            "Back"
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(35.dp)
            )


            // -----------------------------------------------------
            // TITLE
            // -----------------------------------------------------

            Text(
                text = "Forgot Password?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )


            Text(
                text =
                    "Enter your registered email address and we will send you a password reset link.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier
                    .fillMaxWidth(),
                lineHeight = 21.sp
            )


            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )


            // -----------------------------------------------------
            // EMAIL
            // -----------------------------------------------------

            OutlinedTextField(
                value = uiState.email,

                onValueChange = {
                    viewModel.updateEmail(it)
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true,

                enabled =
                    !uiState.isLoading &&
                            !uiState.emailSent,

                label = {
                    Text("Email")
                },

                placeholder = {
                    Text("Enter your email")
                },

                leadingIcon = {

                    Icon(
                        imageVector =
                            Icons.Default.Email,
                        contentDescription =
                            "Email"
                    )
                },

                shape =
                    RoundedCornerShape(14.dp),

                isError =
                    uiState.errorMessage != null
            )


            // -----------------------------------------------------
            // ERROR
            // -----------------------------------------------------

            if (
                uiState.errorMessage != null
            ) {

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        uiState.errorMessage!!,

                    modifier =
                        Modifier.fillMaxWidth(),

                    color =
                        Color(0xFFDC2626),

                    fontSize = 13.sp
                )
            }


            // -----------------------------------------------------
            // SUCCESS
            // -----------------------------------------------------

            if (uiState.emailSent) {

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Text(
                    text =
                        "If an account exists for this email, a password reset link has been sent. Please check your inbox and spam folder.",

                    modifier =
                        Modifier.fillMaxWidth(),

                    color =
                        Color(0xFF15803D),

                    fontSize = 14.sp,

                    lineHeight = 21.sp
                )
            }


            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )


            // -----------------------------------------------------
            // SEND BUTTON
            // -----------------------------------------------------

            Button(
                onClick = {
                    viewModel.sendResetEmail()
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                enabled =
                    !uiState.isLoading &&
                            !uiState.emailSent,

                shape =
                    RoundedCornerShape(14.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2563EB)
                    )
            ) {

                if (uiState.isLoading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(22.dp),

                        color =
                            Color.White,

                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text =
                            "SEND RESET LINK",

                        fontSize =
                            15.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // -----------------------------------------------------
            // BACK TO LOGIN
            // -----------------------------------------------------

            if (uiState.emailSent) {

                Button(
                    onClick = onBack,

                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor =
                                Color(0xFF2563EB)
                        )
                ) {

                    Text(
                        text =
                            "BACK TO LOGIN"
                    )
                }
            }
        }
    }
}
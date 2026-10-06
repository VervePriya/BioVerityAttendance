
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
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ResetPasswordScreen(
    accessToken: String,
    onPasswordUpdated: () -> Unit,
    viewModel: ResetPasswordViewModel =
        viewModel()
) {

    val uiState by
    viewModel.uiState.collectAsState()


    if (uiState.success) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFFF7F8FA)
                    )
                    .padding(28.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text =
                    "Password Updated",

                fontSize =
                    26.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF111827)
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Text(
                text =
                    "Your password has been updated successfully. You can now log in with your new password.",

                fontSize =
                    14.sp,

                lineHeight =
                    21.sp,

                color =
                    Color(0xFF6B7280)
            )


            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )


            Button(
                onClick =
                    onPasswordUpdated,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                shape =
                    RoundedCornerShape(14.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2563EB)
                    )
            ) {

                Text(
                    text =
                        "BACK TO LOGIN",

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        return
    }


    // ---------------------------------------------------------
    // RESET PASSWORD FORM
    // ---------------------------------------------------------

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFFF7F8FA)
                )
                .padding(28.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text =
                "Reset Password",

            fontSize =
                28.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                Color(0xFF111827)
        )


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        Text(
            text =
                "Enter a new password for your account.",

            fontSize =
                14.sp,

            color =
                Color(0xFF6B7280)
        )


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        // -----------------------------------------------------
        // NEW PASSWORD
        // -----------------------------------------------------

        OutlinedTextField(

            value =
                uiState.newPassword,

            onValueChange = {
                viewModel.updateNewPassword(it)
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine =
                true,

            enabled =
                !uiState.isLoading,

            label = {
                Text("New Password")
            },

            visualTransformation =
                PasswordVisualTransformation(),

            shape =
                RoundedCornerShape(14.dp),

            isError =
                uiState.errorMessage != null
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        // -----------------------------------------------------
        // CONFIRM PASSWORD
        // -----------------------------------------------------

        OutlinedTextField(

            value =
                uiState.confirmPassword,

            onValueChange = {
                viewModel.updateConfirmPassword(it)
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine =
                true,

            enabled =
                !uiState.isLoading,

            label = {
                Text("Confirm Password")
            },

            visualTransformation =
                PasswordVisualTransformation(),

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
                    Modifier.height(10.dp)
            )

            Text(
                text =
                    uiState.errorMessage!!,

                modifier =
                    Modifier.fillMaxWidth(),

                color =
                    Color(0xFFDC2626),

                fontSize =
                    13.sp
            )
        }


        Spacer(
            modifier =
                Modifier.height(26.dp)
        )


        // -----------------------------------------------------
        // UPDATE BUTTON
        // -----------------------------------------------------

        Button(

            onClick = {

                viewModel.resetPassword(
                    accessToken
                )
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(54.dp),

            enabled =
                !uiState.isLoading,

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
                        Modifier.height(22.dp),

                    color =
                        Color.White,

                    strokeWidth =
                        2.dp
                )

            } else {

                Text(
                    text =
                        "UPDATE PASSWORD",

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


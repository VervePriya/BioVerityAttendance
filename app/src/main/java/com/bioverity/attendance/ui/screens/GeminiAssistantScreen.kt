
package com.bioverity.attendance.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bioverity.attendance.data.api.ApiClient
import com.bioverity.attendance.data.model.GeminiRequest
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.bioverity.attendance.data.session.AppSession


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiAssistantScreen(
    onBack: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF6F8FB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BioVerity AI Assistant",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "How can I help you?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B63)
            )

            Text(
                text = "Ask a question and get an AI-generated response."
            )

            OutlinedTextField(
                value = question,
                onValueChange = {
                    question = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter your question") },
                placeholder = {
                    Text("Ask BioVerity AI...")
                },
                minLines = 3,
                maxLines = 5
            )

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        answer = ""
                        error = ""


                        try {
                            val accessToken = AppSession.getAccessToken(context)

                            if (accessToken.isNullOrBlank()) {
                                error = "Your login session has expired. Please log in again."
                                return@launch
                            }

                            val response = ApiClient.api.askGemini(
                                GeminiRequest(
                                    prompt = question.trim(),
                                    access_token = accessToken
                                )
                            )

                            if (response.success) {
                                answer = response.answer ?: "No answer received."
                            } else {
                                error = "Unable to generate a response."
                            }



                            if (response.success) {
                                answer = response.answer
                                    ?: "No answer received."
                            } else {
                                error = "Unable to generate a response."
                            }

                        } catch (e: Exception) {
                            error = "Unable to connect to AI. Please try again."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = question.isNotBlank() && !isLoading
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null
                )

                Spacer(Modifier.padding(4.dp))

                Text("Ask Gemini")
            }

            if (isLoading) {
                CircularProgressIndicator()
                Text("Gemini is thinking...")
            }

            if (error.isNotBlank()) {
                Text(
                    text = error,
                    color = Color.Red
                )
            }

            if (answer.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "AI Response",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF173B63)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(text = answer)
                    }
                }
            }
        }
    }
}

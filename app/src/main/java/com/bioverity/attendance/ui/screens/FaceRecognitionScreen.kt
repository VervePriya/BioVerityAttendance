
@file:Suppress("UnsafeOptInUsageError")

package com.bioverity.attendance.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.concurrent.Executors

private const val FACE_API_URL = "http://127.0.0.1:8000"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceRecognitionScreen(
    onBack: () -> Unit,
    onFaceDetected: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var faceCount by remember {
        mutableIntStateOf(0)
    }

    var statusText by remember {
        mutableStateOf(
            "Position your face inside the camera"
        )
    }

    var resultText by remember {
        mutableStateOf("")
    }

    var isVerifying by remember {
        mutableStateOf(false)
    }

    var latestJpeg by remember {
        mutableStateOf<ByteArray?>(null)
    }

    /*
     * ================================================================
     * RECOGNIZED EMPLOYEE DETAILS
     * ================================================================
     *
     * These are stored after successful face verification.
     * They will be used by Check In / Check Out in Step 3.
     */

    var recognizedPersonId by remember {
        mutableStateOf<String?>(null)
    }

    var recognizedPersonName by remember {
        mutableStateOf<String?>(null)
    }

    var recognizedConfidence by remember {
        mutableStateOf(0.0)
    }

    /*
     * ================================================================
     * ATTENDANCE ACTION STATE
     * ================================================================
     */

    var attendanceMessage by remember {
        mutableStateOf("")
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasCameraPermission = granted

            if (!granted) {
                statusText =
                    "Camera permission is required"
            }
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    /*
     * ================================================================
     * FACE VERIFICATION
     * ================================================================
     */

    fun verifyEmployee() {

        if (isVerifying) {
            return
        }

        val jpegBytes = latestJpeg

        if (jpegBytes == null) {

            resultText =
                "No camera frame available"

            return
        }

        if (faceCount != 1) {

            resultText =
                when {

                    faceCount == 0 ->
                        "No face detected"

                    faceCount > 1 ->
                        "Please keep only one person in the camera"

                    else ->
                        "Face not ready"
                }

            return
        }

        isVerifying = true

        resultText = "Verifying..."

        /*
         * Clear any previous attendance state
         */
        recognizedPersonId = null
        recognizedPersonName = null
        recognizedConfidence = 0.0
        attendanceMessage = ""

        Thread {

            try {

                val client =
                    OkHttpClient()

                val imageBody =
                    jpegBytes.toRequestBody(
                        "image/jpeg".toMediaType()
                    )

                val multipartBody =
                    MultipartBody.Builder()
                        .setType(
                            MultipartBody.FORM
                        )
                        .addFormDataPart(
                            "image",
                            "face.jpg",
                            imageBody
                        )
                        .build()

                val request =
                    Request.Builder()
                        .url(
                            "$FACE_API_URL/recognize-face"
                        )
                        .post(multipartBody)
                        .build()

                println(
                    "FACE_API: Sending image to " +
                            "$FACE_API_URL/recognize-face"
                )

                client
                    .newCall(request)
                    .execute()
                    .use { response ->

                        val responseText =
                            response.body.string()

                        println(
                            "FACE_API: HTTP ${response.code}"
                        )

                        println(
                            "FACE_API: Response = " +
                                    responseText
                        )

                        if (!response.isSuccessful) {

                            throw Exception(
                                "HTTP ${response.code}: " +
                                        responseText
                            )
                        }

                        val json =
                            JSONObject(responseText)

                        val recognized =
                            json.optBoolean(
                                "recognized",
                                false
                            )

                        val reason =
                            json.optString(
                                "reason",
                                "unknown"
                            )

                        /*
                         * ====================================================
                         * RECOGNIZED
                         * ====================================================
                         */

                        if (recognized) {

                            val personId =
                                json.optString(
                                    "person_id",
                                    ""
                                )

                            val name =
                                json.optString(
                                    "name",
                                    "Unknown"
                                )

                            val confidence =
                                json.optDouble(
                                    "confidence",
                                    0.0
                                )

                            Handler(
                                Looper.getMainLooper()
                            ).post {

                                /*
                                 * Store recognized employee.
                                 */
                                recognizedPersonId =
                                    personId

                                recognizedPersonName =
                                    name

                                recognizedConfidence =
                                    confidence

                                resultText =
                                    "✓ Verified\n\n" +
                                            "Employee: $name\n" +
                                            "Confidence: ${
                                                String.format(
                                                    Locale.US,
                                                    "%.4f",
                                                    confidence
                                                )
                                            }"

                                attendanceMessage = ""

                                isVerifying = false

                                onFaceDetected()
                            }
                        }

                        /*
                         * ====================================================
                         * BELOW THRESHOLD
                         * ====================================================
                         */

                        else if (
                            reason == "below_threshold"
                        ) {

                            val bestMatchName =
                                json.optString(
                                    "best_match_name",
                                    "Unknown"
                                )

                            val bestMatchScore =
                                json.optDouble(
                                    "best_match_score",
                                    json.optDouble(
                                        "confidence",
                                        0.0
                                    )
                                )

                            Handler(
                                Looper.getMainLooper()
                            ).post {

                                resultText =
                                    "⚠ Not verified\n\n" +
                                            "Best match: " +
                                            bestMatchName +
                                            "\n" +
                                            "Similarity: ${
                                                String.format(
                                                    Locale.US,
                                                    "%.4f",
                                                    bestMatchScore
                                                )
                                            }\n\n" +
                                            "Below recognition threshold"

                                /*
                                 * Do not allow attendance actions
                                 * for an unverified person.
                                 */
                                recognizedPersonId = null
                                recognizedPersonName = null
                                recognizedConfidence = 0.0
                                attendanceMessage = ""

                                isVerifying = false
                            }
                        }

                        /*
                         * ====================================================
                         * OTHER FACE ERRORS
                         * ====================================================
                         */

                        else {

                            Handler(
                                Looper.getMainLooper()
                            ).post {

                                resultText =
                                    when (reason) {

                                        "no_face" ->
                                            "No face detected"

                                        "multiple_faces" ->
                                            "Multiple faces detected"

                                        "no_registered_faces" ->
                                            "No registered employees"

                                        "embedding_failed" ->
                                            "Face embedding failed"

                                        "empty_image" ->
                                            "Empty image received"

                                        "invalid_image" ->
                                            "Invalid image received"

                                        else ->
                                            "Face not recognized\n" +
                                                    "Reason: $reason"
                                    }

                                recognizedPersonId = null
                                recognizedPersonName = null
                                recognizedConfidence = 0.0
                                attendanceMessage = ""

                                isVerifying = false
                            }
                        }
                    }

            } catch (e: Exception) {

                println(
                    "FACE_API ERROR: ${e.message}"
                )

                Handler(
                    Looper.getMainLooper()
                ).post {

                    resultText =
                        "Recognition failed:\n" +
                                "${e.message}"

                    recognizedPersonId = null
                    recognizedPersonName = null
                    recognizedConfidence = 0.0
                    attendanceMessage = ""

                    isVerifying = false
                }
            }

        }.start()
    }

    fun saveAttendance(attendanceType: String) {

        val personId = recognizedPersonId
        val personName = recognizedPersonName

        if (personId.isNullOrBlank() || personName.isNullOrBlank()) {

            attendanceMessage =
                "Employee is not verified"

            return
        }

        attendanceMessage =
            "Saving ${attendanceType.replace("_", " ")}..."

        Thread {

            try {

                val client =
                    OkHttpClient()

                val jsonBody =
                    JSONObject().apply {

                        put(
                            "person_id",
                            personId
                        )

                        put(
                            "person_name",
                            personName
                        )

                        put(
                            "attendance_type",
                            attendanceType
                        )
                    }

                val requestBody =
                    jsonBody
                        .toString()
                        .toRequestBody(
                            "application/json".toMediaType()
                        )

                val request =
                    Request.Builder()
                        .url(
                            "$FACE_API_URL/attendance"
                        )
                        .post(requestBody)
                        .build()

                println(
                    "ATTENDANCE_API: Sending $attendanceType"
                )

                client
                    .newCall(request)
                    .execute()
                    .use { response ->

                        val responseText =
                            response.body.string()

                        println(
                            "ATTENDANCE_API: HTTP ${response.code}"
                        )

                        println(
                            "ATTENDANCE_API: Response = " +
                                    responseText
                        )

                        if (!response.isSuccessful) {

                            throw Exception(
                                "HTTP ${response.code}: " +
                                        responseText
                            )
                        }

                        val json =
                            JSONObject(responseText)

                        val success =
                            json.optBoolean(
                                "success",
                                false
                            )

                        val message =
                            json.optString(
                                "message",
                                ""
                            )

                        Handler(
                            Looper.getMainLooper()
                        ).post {

                            if (success) {

                                attendanceMessage =
                                    "✓ $message"

                            } else {

                                val reason =
                                    json.optString(
                                        "reason",
                                        "Unknown error"
                                    )

                                attendanceMessage =
                                    "Attendance failed:\n$reason"
                            }
                        }
                    }

            } catch (e: Exception) {

                println(
                    "ATTENDANCE_API ERROR: ${e.message}"
                )

                Handler(
                    Looper.getMainLooper()
                ).post {

                    attendanceMessage =
                        "Attendance failed:\n" +
                                "${e.message}"
                }
            }
        }.start()
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Face Verification")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,

                            contentDescription =
                                "Back"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        if (!hasCameraPermission) {

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    "Camera permission is required"
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Button(

                    onClick = {

                        permissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }

                ) {

                    Text("Allow Camera")
                }
            }

        } else {

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
            ) {

                /*
                 * ============================================================
                 * CAMERA
                 * ============================================================
                 */

                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                ) {

                    AndroidView(

                        modifier =
                            Modifier.fillMaxSize(),

                        factory = { ctx ->

                            val previewView =
                                PreviewView(ctx)

                            val cameraProviderFuture =
                                ProcessCameraProvider
                                    .getInstance(ctx)

                            cameraProviderFuture
                                .addListener(

                                    {

                                        val cameraProvider =
                                            cameraProviderFuture
                                                .get()

                                        val preview =
                                            Preview.Builder()
                                                .build()
                                                .also {

                                                    it.surfaceProvider =
                                                        previewView
                                                            .surfaceProvider
                                                }

                                        val faceDetector =
                                            FaceDetection
                                                .getClient(

                                                    FaceDetectorOptions
                                                        .Builder()

                                                        .setPerformanceMode(
                                                            FaceDetectorOptions
                                                                .PERFORMANCE_MODE_FAST
                                                        )

                                                        .setLandmarkMode(
                                                            FaceDetectorOptions
                                                                .LANDMARK_MODE_NONE
                                                        )

                                                        .setClassificationMode(
                                                            FaceDetectorOptions
                                                                .CLASSIFICATION_MODE_NONE
                                                        )

                                                        .build()
                                                )

                                        val imageAnalyzer =
                                            ImageAnalysis.Builder()

                                                .setBackpressureStrategy(
                                                    ImageAnalysis
                                                        .STRATEGY_KEEP_ONLY_LATEST
                                                )

                                                .build()

                                        imageAnalyzer.setAnalyzer(

                                            cameraExecutor

                                        ) { imageProxy ->

                                            processCameraFrame(

                                                imageProxy =
                                                    imageProxy,

                                                faceDetector =
                                                    faceDetector,

                                                onFaceCount = {
                                                    faceCount = it
                                                },

                                                onJpegAvailable = {
                                                    latestJpeg = it
                                                },

                                                onStatus = {
                                                    statusText = it
                                                }
                                            )
                                        }

                                        try {

                                            cameraProvider
                                                .unbindAll()

                                            cameraProvider
                                                .bindToLifecycle(

                                                    lifecycleOwner,

                                                    CameraSelector
                                                        .DEFAULT_FRONT_CAMERA,

                                                    preview,

                                                    imageAnalyzer
                                                )

                                        } catch (e: Exception) {

                                            println(
                                                "CAMERA ERROR: " +
                                                        e.message
                                            )
                                        }

                                    },

                                    ContextCompat
                                        .getMainExecutor(ctx)
                                )

                            previewView
                        }
                    )

                    /*
                     * ========================================================
                     * FACE STATUS
                     * ========================================================
                     */

                    Box(

                        modifier =
                            Modifier
                                .align(
                                    Alignment.TopCenter
                                )
                                .padding(16.dp)
                                .background(
                                    Color.Black.copy(
                                        alpha = 0.65f
                                    ),
                                    RoundedCornerShape(
                                        12.dp
                                    )
                                )
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp
                                )
                    ) {

                        Text(
                            text = statusText,
                            color = Color.White
                        )
                    }
                }

                /*
                 * ============================================================
                 * RESULT + ATTENDANCE ACTIONS
                 * ============================================================
                 */

                Column(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    /*
                     * ========================================================
                     * RECOGNITION RESULT
                     * ========================================================
                     */

                    if (resultText.isNotEmpty()) {

                        Text(

                            text = resultText,

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )
                    }

                    /*
                     * ========================================================
                     * CHECK IN / CHECK OUT
                     * ========================================================
                     *
                     * These buttons are intentionally UI-only for now.
                     * Step 3 will connect them to the Python API.
                     */

                    if (
                        recognizedPersonId != null &&
                        recognizedPersonName != null
                    ) {

                        Text(
                            text =
                                "Attendance for " +
                                        recognizedPersonName,

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Button(

                            onClick = {

                                /*
                                 * Step 3:
                                 * Call /attendance API
                                 * with CHECK_IN.
                                 */
                                saveAttendance("CHECK_IN")

                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                        ) {

                            Text("Check In")
                        }

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        OutlinedButton(

                            onClick = {

                                /*
                                 * Step 3:
                                 * Call /attendance API
                                 * with CHECK_OUT.
                                 */
                                saveAttendance("CHECK_OUT")

                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                        ) {

                            Text("Check Out")
                        }

                        if (
                            attendanceMessage.isNotEmpty()
                        ) {

                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Text(
                                text =
                                    attendanceMessage,

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium
                            )
                        }

                    } else {

                        /*
                         * ====================================================
                         * VERIFY BUTTON
                         * ====================================================
                         */

                        Button(

                            onClick = {
                                verifyEmployee()
                            },

                            enabled =
                                !isVerifying &&
                                        faceCount == 1,

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                        ) {

                            Text(

                                if (isVerifying)
                                    "Verifying..."
                                else
                                    "Verify Employee"
                            )
                        }
                    }
                }
            }
        }
    }
}


/* ========================================================================
 * CAMERA FRAME PROCESSING
 * ======================================================================== */

private fun processCameraFrame(

    imageProxy: ImageProxy,

    faceDetector:
    com.google.mlkit.vision.face.FaceDetector,

    onFaceCount: (Int) -> Unit,

    onJpegAvailable:
        (ByteArray) -> Unit,

    onStatus: (String) -> Unit
) {

    val mediaImage =
        imageProxy.image

    if (mediaImage == null) {

        imageProxy.close()

        return
    }

    val rotationDegrees =
        imageProxy.imageInfo.rotationDegrees

    val inputImage =
        InputImage.fromMediaImage(
            mediaImage,
            rotationDegrees
        )

    val jpegBytes =
        imageProxyToJpeg(
            imageProxy,
            rotationDegrees
        )

    if (jpegBytes != null) {

        onJpegAvailable(
            jpegBytes
        )
    }

    faceDetector

        .process(inputImage)

        .addOnSuccessListener { faces ->

            onFaceCount(
                faces.size
            )

            when {

                faces.isEmpty() ->

                    onStatus(
                        "No face detected"
                    )

                faces.size > 1 ->

                    onStatus(
                        "Multiple faces detected"
                    )

                else ->

                    onStatus(
                        "Face detected - ready to verify"
                    )
            }
        }

        .addOnFailureListener { e ->

            onStatus(
                "Face detection error: " +
                        e.message
            )
        }

        .addOnCompleteListener {

            imageProxy.close()
        }
}


/* ========================================================================
 * YUV -> JPEG
 * ======================================================================== */

private fun imageProxyToJpeg(

    imageProxy: ImageProxy,

    rotationDegrees: Int

): ByteArray? {

    return try {

        if (
            imageProxy.format !=
            ImageFormat.YUV_420_888
        ) {

            return null
        }

        val image =
            imageProxy.image
                ?: return null

        val yBuffer =
            image.planes[0].buffer

        val uBuffer =
            image.planes[1].buffer

        val vBuffer =
            image.planes[2].buffer

        val ySize =
            yBuffer.remaining()

        val uSize =
            uBuffer.remaining()

        val vSize =
            vBuffer.remaining()

        val nv21 =
            ByteArray(
                ySize + uSize + vSize
            )

        yBuffer.get(
            nv21,
            0,
            ySize
        )

        val uBytes =
            ByteArray(uSize)

        val vBytes =
            ByteArray(vSize)

        uBuffer.get(
            uBytes
        )

        vBuffer.get(
            vBytes
        )

        var position =
            ySize

        var i = 0

        while (i < vSize) {

            nv21[position++] =
                vBytes[i]

            if (i < uSize) {

                nv21[position++] =
                    uBytes[i]
            }

            i++
        }

        val yuvImage =
            YuvImage(

                nv21,

                ImageFormat.NV21,

                image.width,

                image.height,

                null
            )

        val outputStream =
            ByteArrayOutputStream()

        yuvImage.compressToJpeg(

            Rect(
                0,
                0,
                image.width,
                image.height
            ),

            90,

            outputStream
        )

        outputStream.toByteArray()

    } catch (e: Exception) {

        println(
            "JPEG conversion error: " +
                    e.message
        )

        null
    }
}


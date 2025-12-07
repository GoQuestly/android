package com.goquestly.presentation.task

import android.Manifest
import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.goquestly.R
import com.goquestly.domain.model.QuestTask
import com.goquestly.domain.model.QuizAnswer
import com.goquestly.domain.model.QuizQuestion
import com.goquestly.domain.model.TaskType
import com.goquestly.presentation.core.components.FullScreenLoader
import com.goquestly.presentation.core.components.button.PrimaryButton
import com.goquestly.presentation.core.components.textField.AppTextField
import com.goquestly.presentation.core.preview.ThemePreview
import com.goquestly.presentation.core.theme.GoquestlyTheme
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: (sessionId: Int, pointId: Int, scoreEarned: Int, maxScore: Int, passed: Boolean, isPhotoTask: Boolean) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val originalSoftInputMode = window?.attributes?.softInputMode
        @Suppress("DEPRECATION")
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        onDispose {
            @Suppress("DEPRECATION")
            originalSoftInputMode?.let {
                window.setSoftInputMode(it)
            }
        }
    }

    BackHandler(enabled = state.isTaskStarted, onBack = {})

    LaunchedEffect(state.submitResult) {
        state.submitResult?.let { result ->
            val isPhotoTask = state.task is QuestTask.Photo
            onNavigateToSuccess(
                state.sessionId,
                state.pointId,
                result.scoreEarned ?: 0,
                result.maxScore ?: 0,
                result.passed,
                isPhotoTask
            )
        }
    }

    LaunchedEffect(state.isTaskExpired) {
        if (state.isTaskExpired) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.pointName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    if (!state.isTaskStarted) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    if (state.isTaskStarted && state.remainingTimeSeconds > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (state.remainingTimeSeconds < 60) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTime(state.remainingTimeSeconds),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (state.remainingTimeSeconds < 60) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorContent(
                        error = state.error!!,
                        onRetry = viewModel::retry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.task != null -> {
                    TaskContent(
                        task = state.task!!,
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskContent(
    task: QuestTask,
    state: TaskState,
    viewModel: TaskViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isTaskStarted) {
            item {
                Text(
                    text = task.description,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        when (task) {
            is QuestTask.CodeWord -> {
                item {
                    CodeWordTaskContent(
                        state = state,
                        onCodeWordChange = viewModel::updateCodeWord,
                        onSubmit = viewModel::submitCodeWord
                    )
                }
            }

            is QuestTask.Quiz -> {
                val currentQuestion = task.questions
                    .sortedBy { it.orderNumber }
                    .getOrNull(state.currentQuestionIndex)

                if (currentQuestion != null) {
                    item {
                        QuizQuestionItem(
                            question = currentQuestion,
                            questionNumber = state.currentQuestionIndex + 1,
                            totalQuestions = task.questions.size,
                            selectedAnswerIds = state.quizAnswers[currentQuestion.quizQuestionId]
                                ?: emptyList(),
                            onAnswerSelected = { answerId ->
                                viewModel.selectQuizAnswer(
                                    currentQuestion.quizQuestionId,
                                    answerId,
                                    currentQuestion.isMultipleAnswer
                                )
                            },
                            onSubmit = {
                                viewModel.submitQuizAnswer(currentQuestion.quizQuestionId)
                            },
                            isSubmitting = state.isSubmitting,
                            isTaskStarted = state.isTaskStarted
                        )
                    }
                }
            }

            is QuestTask.Photo -> {
                item {
                    PhotoTaskContent(
                        task = task,
                        state = state,
                        onPhotoSelected = viewModel::setPhoto,
                        onSubmit = viewModel::submitPhoto
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeWordTaskContent(
    state: TaskState,
    onCodeWordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.isSubmitting) {
            FullScreenLoader(modifier = Modifier.fillMaxWidth())
        }

        if (!state.isTaskStarted) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(R.string.starting_task),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = stringResource(R.string.provide_answer_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AppTextField(
                value = state.codeWordInput,
                onValueChange = onCodeWordChange,
                placeholder = stringResource(R.string.type_your_answer_here),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting
            )

            PrimaryButton(
                text = stringResource(R.string.submit_answer),
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.codeWordInput.isNotBlank() && !state.isSubmitting,
            )
        }
    }
}

@Composable
private fun QuizQuestionItem(
    question: QuizQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswerIds: List<Int>,
    onAnswerSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean,
    isTaskStarted: Boolean
) {
    if (isSubmitting) {
        FullScreenLoader(modifier = Modifier.fillMaxWidth())
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isTaskStarted) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                )
                Text(
                    text = stringResource(R.string.starting_task),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.question_number_of_total,
                        questionNumber,
                        totalQuestions
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (question.isMultipleAnswer) {
                    Text(
                        text = stringResource(R.string.select_all_that_apply),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    question.answers.forEach { answer ->
                        val isSelected = answer.quizAnswerId in selectedAnswerIds

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = { onAnswerSelected(answer.quizAnswerId) },
                                    role = if (question.isMultipleAnswer) Role.Checkbox else Role.RadioButton
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (question.isMultipleAnswer) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null
                                    )
                                } else {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = answer.answer,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                PrimaryButton(
                    text = stringResource(R.string.submit_answer),
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswerIds.isNotEmpty() && !isSubmitting,
                )
            }
        }
    }
}

@Composable
private fun PhotoTaskContent(
    task: QuestTask.Photo,
    state: TaskState,
    onPhotoSelected: (String, File) -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            val file = tempPhotoFile!!
            onPhotoSelected(file.absolutePath, file)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            tempPhotoFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            cameraLauncher.launch(uri)
        }
    }

    if (state.isSubmitting) {
        FullScreenLoader(modifier = Modifier.fillMaxWidth())
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!state.isTaskStarted) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(R.string.starting_task),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.photoUri != null) {
                Text(
                    text = stringResource(R.string.preview_of_submission),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Image(
                    painter = rememberAsyncImagePainter(state.photoUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                PrimaryButton(
                    text = stringResource(R.string.submit_photo),
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.no_photo_uploaded_yet),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PrimaryButton(
                    text = stringResource(R.string.take_photo),
                    onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.error_oops),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        PrimaryButton(
            text = stringResource(R.string.try_again),
            onClick = onRetry
        )
    }
}

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}

@ThemePreview
@Composable
private fun CodeWordTaskPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                CodeWordTaskContent(
                    state = TaskState(
                        isTaskStarted = true,
                        codeWordInput = "SECRET",
                        remainingTimeSeconds = 180
                    ),
                    onCodeWordChange = {},
                    onSubmit = {}
                )
            }
        }
    }
}

@ThemePreview
@Composable
private fun QuizTaskPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                QuizQuestionItem(
                    question = QuizQuestion(
                        quizQuestionId = 1,
                        question = "What is the capital of Ukraine?",
                        orderNumber = 1,
                        scorePointsCount = 5,
                        isMultipleAnswer = false,
                        answers = listOf(
                            QuizAnswer(1, "Kyiv"),
                            QuizAnswer(2, "Lviv"),
                            QuizAnswer(3, "Odesa"),
                            QuizAnswer(4, "Kharkiv")
                        )
                    ),
                    questionNumber = 1,
                    totalQuestions = 3,
                    selectedAnswerIds = listOf(1),
                    onAnswerSelected = {},
                    onSubmit = {},
                    isSubmitting = false,
                    isTaskStarted = true
                )
            }
        }
    }
}

@ThemePreview
@Composable
private fun QuizMultipleAnswerPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                QuizQuestionItem(
                    question = QuizQuestion(
                        quizQuestionId = 2,
                        question = "Which of these are programming languages?",
                        orderNumber = 2,
                        scorePointsCount = 10,
                        isMultipleAnswer = true,
                        answers = listOf(
                            QuizAnswer(1, "Kotlin"),
                            QuizAnswer(2, "JavaScript"),
                            QuizAnswer(3, "HTML"),
                            QuizAnswer(4, "Python")
                        )
                    ),
                    questionNumber = 2,
                    totalQuestions = 3,
                    selectedAnswerIds = listOf(1, 2, 4),
                    onAnswerSelected = {},
                    onSubmit = {},
                    isSubmitting = false,
                    isTaskStarted = true
                )
            }
        }
    }
}

@ThemePreview
@Composable
private fun PhotoTaskPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                PhotoTaskContent(
                    task = QuestTask.Photo(
                        questTaskId = 3,
                        taskType = TaskType.PHOTO,
                        description = "Take a photo of the monument in the city center",
                        maxDurationSeconds = 600,
                        isRequiredForNextPoint = true,
                        scorePointsCount = 15
                    ),
                    state = TaskState(
                        isTaskStarted = true,
                        photoUri = null,
                        remainingTimeSeconds = 420
                    ),
                    onPhotoSelected = { _, _ -> },
                    onSubmit = {}
                )
            }
        }
    }
}

@ThemePreview
@Composable
private fun TaskStartingPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                CodeWordTaskContent(
                    state = TaskState(
                        isTaskStarted = false,
                        codeWordInput = "",
                        remainingTimeSeconds = 300
                    ),
                    onCodeWordChange = {},
                    onSubmit = {}
                )
            }
        }
    }
}

@ThemePreview
@Composable
private fun TaskErrorPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            ErrorContent(
                error = "Failed to load task. Please check your internet connection and try again.",
                onRetry = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@ThemePreview
@Composable
private fun QuizLoadingPreview() {
    GoquestlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                QuizQuestionItem(
                    question = QuizQuestion(
                        quizQuestionId = 1,
                        question = "What is the capital of Ukraine?",
                        orderNumber = 1,
                        scorePointsCount = 5,
                        isMultipleAnswer = false,
                        answers = listOf(
                            QuizAnswer(1, "Kyiv"),
                            QuizAnswer(2, "Lviv"),
                            QuizAnswer(3, "Odesa"),
                            QuizAnswer(4, "Kharkiv")
                        )
                    ),
                    questionNumber = 1,
                    totalQuestions = 3,
                    selectedAnswerIds = emptyList(),
                    onAnswerSelected = {},
                    onSubmit = {},
                    isSubmitting = false,
                    isTaskStarted = false
                )
            }
        }
    }
}

package pelkidev.com.mx.findout.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pelkidev.com.mx.findout.R
import pelkidev.com.mx.findout.domain.LevelCatalog
import pelkidev.com.mx.findout.domain.LevelConfig

@Composable
fun FindOutScreen(
    onExit: () -> Unit,
    viewModel: FindOutViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state.screen) {
        FindOutScreenState.Menu -> FindOutMenuScreen(
            onExit = onExit,
            levels = LevelCatalog.all,
            onStartLevel = viewModel::startLevel,
        )
        FindOutScreenState.Playing -> FindOutPlayScreen(
            state = state,
            onBackToMenu = viewModel::backToMenu,
            onTapImage = viewModel::onImageTap,
            onRestart = viewModel::restartLevel,
            onNext = viewModel::goNextLevel,
            onClearLastFound = viewModel::clearLastFoundAnim,
        )
    }
}

@Composable
private fun FindOutMenuScreen(
    onExit: () -> Unit,
    levels: List<LevelConfig>,
    onStartLevel: (LevelConfig) -> Unit,
) {
    val gradient = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.findout_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.findout_menu_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    levels.forEachIndexed { index, level ->
                        if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                        val label = when (level.levelId) {
                            "nivel_1" -> stringResource(R.string.findout_start_level_1)
                            "nivel_2" -> stringResource(R.string.findout_start_level_2)
                            else -> level.levelId
                        }
                        if (index == 0) {
                            Button(
                                onClick = { onStartLevel(level) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Text(label)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onStartLevel(level) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
                .shadow(3.dp, CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.findout_back),
            )
        }
    }
}

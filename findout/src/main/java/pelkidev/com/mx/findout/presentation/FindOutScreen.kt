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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pelkidev.com.mx.findout.R
import pelkidev.com.mx.findout.domain.LevelCatalog

@Composable
fun FindOutScreen(
    onExit: () -> Unit,
    viewModel: FindOutViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state.screen) {
        FindOutScreenState.Menu -> FindOutMenuScreen(
            onExit = onExit,
            onStart = { viewModel.startLevel(LevelCatalog.nivel1) },
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
    onStart: () -> Unit,
) {
    // Column a pantalla completa primero; el botón atrás se dibuja después (z-order).
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.findout_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.findout_menu_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(0.7f),
            ) {
                Text(stringResource(R.string.findout_start_level))
            }
        }

        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.findout_back),
            )
        }
    }
}

package pelkidev.com.mx.findout

import androidx.compose.runtime.Composable
import pelkidev.com.mx.findout.presentation.FindOutScreen
import pelkidev.com.mx.findout.ui.theme.FindOutTheme
import pelkidev.com.mx.minijuegos.sdk.GameModule

object FindOutGameModule : GameModule {
    override val id: String = "findout"
    override val title: String = "Find Out"
    override val description: String = "Encuentra todos los objetos ocultos en la imagen"

    @Composable
    override fun Entry(onExit: () -> Unit) {
        FindOutTheme {
            FindOutScreen(onExit = onExit)
        }
    }
}

package com.pixatrip1984.germanfather.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    state: MainScreenState,
    onRequestNotifications: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    showDebugTestButton: Boolean = false,
    onTestNextAlarm: () -> Unit = {},
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "GERMAN FATHER",
                    style = MaterialTheme.typography.headlineMedium,
                )

                Text(
                    text = "Actual: " + state.currentTask,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = buildString {
                        append("Siguiente: ")
                        append(state.nextTask)
                        state.nextTaskTime?.let {
                            append(" · ")
                            append(it)
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Horario de hoy", style = MaterialTheme.typography.titleMedium)

                if (state.timeline.isEmpty()) {
                    Text("SIN_HORARIO")
                } else {
                    state.timeline.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(row.time)
                            Text(row.taskId)
                        }
                    }
                }

                if (showDebugTestButton) {
                    Button(onClick = onTestNextAlarm) {
                        Text("PROBAR ALARMA SIGUIENTE")
                    }
                }

                if (!state.notificationsGranted) {
                    SetupSection(
                        text = "Se requieren notificaciones para mostrar alarmas.",
                        actionLabel = "Permitir notificaciones",
                        onAction = onRequestNotifications,
                    )
                }

                if (!state.fullScreenIntentGranted) {
                    SetupSection(
                        text = "Se requiere acceso de alarma a pantalla completa.",
                        actionLabel = "Abrir configuración",
                        onAction = onOpenFullScreenSettings,
                    )
                }

                if (!state.exactAlarmAvailable) {
                    Text(
                        text = "Configuración requerida: las alarmas exactas no están disponibles.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } else if (!state.schedulerArmed) {
                    Text(
                        text = "La próxima alarma todavía no está armada.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupSection(
    text: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text)
        Button(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

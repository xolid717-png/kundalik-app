package uz.kundalik.app.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uz.kundalik.app.ApiClient
import uz.kundalik.app.ApiException
import uz.kundalik.app.DailyEntry
import uz.kundalik.app.SubjectAverage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(token: String, onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    val titles = listOf("Kunlik baholar", "Davr o'rtachasi", "Dars jadvali")

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Kundalik") },
            actions = { TextButton(onClick = onLogout) { Text("Chiqish") } }
        )
        TabRow(selectedTabIndex = tab) {
            titles.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
            }
        }
        when (tab) {
            0 -> DailyMarksTab(token)
            1 -> PeriodMarksTab(token)
            2 -> ScheduleTab(token)
        }
    }
}

private fun sessionExpiredMessage(e: Exception): String =
    if (e is ApiException && e.message == "SESSION_EXPIRED")
        "Sessiya muddati tugagan. Ilovadan chiqib, qayta kiring."
    else
        "Ma'lumot olinmadi. Internetni yoki serverni tekshiring."

@Composable
private fun DailyMarksTab(token: String) {
    var entries by remember { mutableStateOf<List<DailyEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        try {
            entries = ApiClient.dailyMarks(token).entries
        } catch (e: Exception) {
            error = sessionExpiredMessage(e)
        }
        loading = false
    }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> Text(error!!)
            entries.isEmpty() -> Text("Baholar topilmadi.")
            else -> LazyColumn {
                items(entries) { e ->
                    ListItem(
                        headlineContent = { Text(e.subject) },
                        supportingContent = { Text(e.day) },
                        trailingContent = {
                            Text(e.grade, style = MaterialTheme.typography.titleLarge)
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun PeriodMarksTab(token: String) {
    var subjects by remember { mutableStateOf<List<SubjectAverage>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        try {
            subjects = ApiClient.periodMarks(token).subjects
        } catch (e: Exception) {
            error = sessionExpiredMessage(e)
        }
        loading = false
    }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> Text(error!!)
            subjects.isEmpty() -> Text("Ma'lumot topilmadi.")
            else -> LazyColumn {
                items(subjects) { s ->
                    ListItem(
                        headlineContent = { Text(s.subject) },
                        trailingContent = {
                            Text(s.average, style = MaterialTheme.typography.titleMedium)
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun ScheduleTab(token: String) {
    var text by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        try {
            text = ApiClient.schedule(token).schedule_text
        } catch (e: Exception) {
            error = sessionExpiredMessage(e)
        }
        loading = false
    }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> Text(error!!)
            else -> Text(text)
        }
    }
}

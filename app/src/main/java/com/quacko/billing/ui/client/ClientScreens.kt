package com.quacko.billing.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quacko.billing.data.Repository
import com.quacko.billing.data.model.CallOrigin
import com.quacko.billing.data.model.CallRecord
import com.quacko.billing.data.model.ClientBillingSummary
import com.quacko.billing.data.model.ReportFilter
import com.quacko.billing.data.model.Session
import com.quacko.billing.ui.common.CappedPill
import com.quacko.billing.ui.common.OriginPill
import com.quacko.billing.ui.common.StatTile
import com.quacko.billing.ui.common.minutesLabel
import com.quacko.billing.ui.common.money
import com.quacko.billing.ui.theme.QuackoAmber
import com.quacko.billing.ui.theme.QuackoGreen
import com.quacko.billing.ui.theme.QuackoRed
import com.quacko.billing.ui.theme.QuackoTeal
import com.quacko.billing.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    session: Session,
    repo: Repository,
    onLogout: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    val clientId = session.clientId ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(session.clientName ?: "My account", maxLines = 1)
                        Text(
                            "Client ID $clientId",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = QuackoTeal,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Billing") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Call history") })
            }
            when (tab) {
                0 -> ClientBillingTab(clientId, repo)
                else -> CallHistoryTab(ReportFilter(clientId = clientId), repo, showClient = false)
            }
        }
    }
}

@Composable
private fun ClientBillingTab(clientId: String, repo: Repository) {
    val summary by produceState<ClientBillingSummary?>(initialValue = null, clientId) {
        value = repo.clientSummary(clientId)
    }
    if (summary == null) {
        LoadingBox(); return
    }
    val s = summary!!
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = QuackoTeal)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Amount due", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
                Text(
                    money(s.amount),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${s.totalCalls} calls · ${minutesLabel(s.totalMinutes)} · ${money(s.ratePerMinute)}/min",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Web minutes", minutesLabel(s.webMinutes), QuackoTeal, Modifier.weight(1f))
            StatTile("800 minutes", minutesLabel(s.phoneMinutes), QuackoAmber, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Web calls", s.webCalls.toString(), QuackoTeal, Modifier.weight(1f))
            StatTile("800 calls", s.phoneCalls.toString(), QuackoAmber, Modifier.weight(1f))
        }
        if (s.cappedCalls > 0) {
            Spacer(Modifier.height(12.dp))
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = QuackoRed.copy(alpha = 0.12f))
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(0.dp))
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("${s.cappedCalls} capped call(s)", fontWeight = FontWeight.SemiBold, color = QuackoRed)
                        Text(
                            "Calls over 120 min are billed at the cap.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CallHistoryTab(filter: ReportFilter, repo: Repository, showClient: Boolean) {
    val calls by produceState<List<CallRecord>?>(initialValue = null, filter) {
        value = repo.calls(filter)
    }
    if (calls == null) { LoadingBox(); return }
    val list = calls!!
    if (list.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No calls found", color = TextMuted) }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(list, key = { it.id }) { CallRow(it, showClient) }
    }
}

@Composable
fun CallRow(c: CallRecord, showClient: Boolean) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (showClient) {
                    Text(c.clientName, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                Text(c.date, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${c.language} · ${c.interpreter}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (c.capped) CappedPill()
                    OriginPill(c.origin == CallOrigin.PHONE_800)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    minutesLabel(c.minutes),
                    fontWeight = FontWeight.Bold,
                    color = if (c.capped) QuackoRed else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
}

@Composable
fun LoadingBox() {
    Box(Modifier.fillMaxSize().height(240.dp), Alignment.Center) {
        CircularProgressIndicator(color = QuackoTeal)
    }
}

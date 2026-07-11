package com.quacko.billing.ui.admin

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quacko.billing.data.DemoData
import com.quacko.billing.data.Repository
import com.quacko.billing.data.model.AdminTotals
import com.quacko.billing.data.model.ClientBillingSummary
import com.quacko.billing.data.model.ReportFilter
import com.quacko.billing.data.model.Session
import com.quacko.billing.ui.client.CallHistoryTab
import com.quacko.billing.ui.client.LoadingBox
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
fun AdminHomeScreen(
    session: Session,
    repo: Repository,
    onLogout: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var clientFilter by remember { mutableStateOf<String?>(null) } // null = all
    val filter = ReportFilter(clientId = clientFilter)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin console")
                        Text(
                            "Signed in as ${session.username}",
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
            ClientFilterDropdown(clientFilter) { clientFilter = it }
            TotalsRow(filter, repo)
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Billing by client") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Call detail") })
            }
            when (tab) {
                0 -> BillingByClientTab(filter, repo)
                else -> CallHistoryTab(filter, repo, showClient = true)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientFilterDropdown(selected: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = selected?.let { id ->
        DemoData.clients.firstOrNull { it.id == id }?.let { "${it.name} ($id)" } ?: id
    } ?: "All clients"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Client filter") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("All clients") }, onClick = { onSelect(null); expanded = false })
            DemoData.clients.forEach { c ->
                DropdownMenuItem(
                    text = { Text("${c.name} (${c.id})") },
                    onClick = { onSelect(c.id); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun TotalsRow(filter: ReportFilter, repo: Repository) {
    val totals by produceState<AdminTotals?>(initialValue = null, filter) {
        value = repo.adminTotals(filter)
    }
    val t = totals
    if (t == null) {
        Box(Modifier.fillMaxWidth().height(90.dp), Alignment.Center) { LoadingBox() }
        return
    }
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Billed", money(t.totalAmount), QuackoGreen, Modifier.weight(1f))
            StatTile("Minutes", minutesLabel(t.totalMinutes), QuackoTeal, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Calls", t.totalCalls.toString(), QuackoTeal, Modifier.weight(1f))
            StatTile("Clients", t.totalClients.toString(), QuackoTeal, Modifier.weight(1f))
            StatTile("Capped", t.cappedCalls.toString(), if (t.cappedCalls > 0) QuackoRed else QuackoTeal, Modifier.weight(1f))
        }
        if (t.cappedCalls > 0) {
            Spacer(Modifier.height(10.dp))
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = QuackoRed.copy(alpha = 0.12f))
            ) {
                Text(
                    "⚠ ${t.cappedCalls} call(s) exceeded the 120-min cap and were billed at the cap.",
                    color = QuackoRed,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun BillingByClientTab(filter: ReportFilter, repo: Repository) {
    val summaries by produceState<List<ClientBillingSummary>?>(initialValue = null, filter) {
        value = repo.summaries(filter)
    }
    if (summaries == null) { LoadingBox(); return }
    val list = summaries!!
    if (list.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No data", color = TextMuted) }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(list, key = { it.clientId }) { ClientSummaryRow(it) }
    }
}

@Composable
private fun ClientSummaryRow(s: ClientBillingSummary) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(s.clientName, fontWeight = FontWeight.SemiBold)
                Text(
                    "ID ${s.clientId} · ${s.totalCalls} calls · ${minutesLabel(s.totalMinutes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    "Web ${minutesLabel(s.webMinutes)} · 800 ${minutesLabel(s.phoneMinutes)}" +
                        if (s.cappedCalls > 0) " · ⚠ ${s.cappedCalls} capped" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (s.cappedCalls > 0) QuackoRed else TextMuted
                )
            }
            Text(money(s.amount), fontWeight = FontWeight.Bold, color = QuackoGreen)
        }
    }
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
}

package com.debuggers.reloop
import androidx.compose.ui.text.style.TextOverflow
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// ─────────────────────────────────────────────────────────────────────────────
// NETWORK CONFIG
// DEPLOYED on Render — works from emulator AND physical device
// For local development: "http://10.0.2.2:8000" (emulator) or your LAN IP
// ─────────────────────────────────────────────────────────────────────────────
private const val BASE_URL = "https://reloop-ui.onrender.com"
private const val API_KEY  = "K4ikJWBZZc5_3A2U1G74zr6wOial3tbiv17LzcJB8MM"

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN TOKENS
// ─────────────────────────────────────────────────────────────────────────────
private val Green      = Color(0xFF153D30)
private val GreenLight = Color(0xFFE7EFE8)
private val Lime       = Color(0xFFD8F36A)
private val WarmWhite  = Color(0xFFF5F4EE)
private val DarkGreen  = Color(0xFF102F25)
private val Ink        = Color(0xFF252D29)
private val Muted      = Color(0xFF69746D)
private val Page       = WarmWhite
private val Line       = Color(0xFFE2E5DD)
private val Amber      = Color(0xFF98631C)
private val AmberBg    = Color(0xFFFDF2E4)
private val ErrorRed   = Color(0xFFB54740)
private val ErrorBg    = Color(0xFFFBECEC)

// ─────────────────────────────────────────────────────────────────────────────
// UI DATA MODEL
// ─────────────────────────────────────────────────────────────────────────────
private data class MaterialItem(
    val id: Int,
    val name: String,
    val category: String,
    val quantity: String,
    val location: String,
    val use: String,
    val description: String,
    val isLive: Boolean = false   // true = came from API, false = demo/local
)

// ─────────────────────────────────────────────────────────────────────────────
// API RESPONSE MODELS
// ─────────────────────────────────────────────────────────────────────────────
private data class GeoMatch(
    val buyerName: String,
    val industry: String,
    val city: String,
    val state: String,
    val distanceKm: Double,
    val quantitySufficient: Boolean,
    val quantityRequired: Double,
    val reuseApplication: String,
    val reasons: List<String>,
    val verificationRequired: Boolean,
    val matchStatus: String
)

private data class GeoMatchesResult(
    val sellerName: String,
    val sellerLocation: String,
    val totalMatches: Int,
    val matches: List<GeoMatch>,
    val dataType: String
)

// ─────────────────────────────────────────────────────────────────────────────
// DEMO FALLBACK DATA
// ─────────────────────────────────────────────────────────────────────────────
private val demoMaterials = listOf(
    MaterialItem(
        1, "Fly Ash", "Mineral by-product",
        "120 tonnes", "Mohali, Punjab",
        "Cement manufacturing",
        "Industrial residue suitable for cement or brick production, subject to quality verification.",
        isLive = false
    ),
    MaterialItem(
        2, "Foundry Sand", "Foundry by-product",
        "48 tonnes", "Chandigarh region",
        "Construction applications",
        "Used foundry sand. Suitability depends on composition and intended application.",
        isLive = false
    ),
    MaterialItem(
        3, "Metal Scrap", "Metal",
        "5 tonnes", "Ludhiana, Punjab",
        "Metal recycling",
        "Ferrous scrap for recovery and recycling. Grade and composition require confirmation.",
        isLive = false
    ),
    MaterialItem(
        4, "Process Water", "Process material",
        "Availability varies", "Northern region",
        "Industrial reuse",
        "Process-water reuse opportunity. Quality testing required before any reuse.",
        isLive = false
    )
)

// Demo sellers for the geo matching demonstration
private val demoSellers = listOf(
    "FAC001" to "Rajpura Thermal Hub",
    "FAC007" to "Bathinda Circular Materials Hub"
)

// City coordinates for Punjab/Haryana (for forms)
private val cityCoordinates = mapOf(
    "Rajpura, Punjab"         to Pair(30.478, 76.593),
    "Bathinda, Punjab"        to Pair(30.211, 74.945),
    "Ludhiana, Punjab"        to Pair(30.901, 75.857),
    "Patiala, Punjab"         to Pair(30.340, 76.387),
    "Chandigarh"              to Pair(30.733, 76.779),
    "Mohali, Punjab"          to Pair(30.705, 76.718),
    "Amritsar, Punjab"        to Pair(31.634, 74.872),
    "Mandi Gobindgarh, Punjab" to Pair(30.664, 76.291),
    "Rupnagar, Punjab"        to Pair(30.966, 76.523),
    "Ambala, Haryana"         to Pair(30.378, 76.777),
    "Panipat, Haryana"        to Pair(29.391, 76.964),
    "Kurukshetra, Haryana"    to Pair(29.970, 76.878),
    "Yamunanagar, Haryana"    to Pair(30.129, 77.267)
)

// ─────────────────────────────────────────────────────────────────────────────
// NETWORK LAYER  (zero new dependencies — uses android.jar built-ins)
// ─────────────────────────────────────────────────────────────────────────────

private suspend fun apiGet(path: String, auth: Boolean = false): JSONObject? =
    withContext(Dispatchers.IO) {
        try {
            val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            if (auth) conn.setRequestProperty("X-API-Key", API_KEY)
            conn.connectTimeout = 8000
            conn.readTimeout   = 15000
            if (conn.responseCode == 200)
                conn.inputStream.bufferedReader().readText().let { JSONObject(it) }
            else null
        } catch (e: Exception) { null }
    }

private suspend fun apiPost(path: String, body: JSONObject, auth: Boolean = false): Pair<Int, JSONObject?> =
    withContext(Dispatchers.IO) {
        try {
            val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            if (auth) conn.setRequestProperty("X-API-Key", API_KEY)
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout   = 15000
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            val code   = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val json   = stream?.bufferedReader()?.readText()?.let { JSONObject(it) }
            Pair(code, json)
        } catch (e: Exception) { Pair(-1, null) }
    }

private suspend fun fetchLiveMaterials(): List<MaterialItem> {
    val json  = apiGet("/materials", auth = true) ?: return emptyList()
    val array = json.optJSONArray("materials") ?: return emptyList()
    val list  = mutableListOf<MaterialItem>()
    var localId = 1000
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val qty    = obj.optDouble("quantity_tonnes", 0.0)
        val city   = obj.optString("city", "")
        val state  = obj.optString("state", "")
        list.add(MaterialItem(
            id          = localId++,
            name        = obj.optString("material_name", "Unknown material"),
            category    = obj.optString("source_industry", "Industrial by-product"),
            quantity    = "${qty.toInt()} tonnes",
            location    = if (city.isNotEmpty()) "$city, $state" else state,
            use         = "Industrial reuse",
            description = "Live listing. Material quality and documentation are subject to independent verification.",
            isLive      = true
        ))
    }
    return list
}

private suspend fun postMaterial(body: JSONObject): Pair<Boolean, String> {
    val (code, json) = apiPost("/materials", body, auth = true)
    return when {
        code == 201 -> Pair(true, json?.optString("message") ?: "Listing created.")
        code == 422 -> Pair(false, "Validation error — check required fields.")
        code == -1  -> Pair(false, "Could not reach server. Check BASE_URL and network.")
        else        -> Pair(false, "Server error $code.")
    }
}

private suspend fun postRequirement(body: JSONObject): Pair<Boolean, String> {
    val (code, json) = apiPost("/requirements", body, auth = true)
    return when {
        code == 201 -> Pair(true, json?.optString("message") ?: "Requirement submitted.")
        code == 422 -> Pair(false, "Validation error — check required fields.")
        code == -1  -> Pair(false, "Could not reach server. Check BASE_URL and network.")
        else        -> Pair(false, "Server error $code.")
    }
}

private suspend fun fetchGeoMatches(sellerId: String): GeoMatchesResult? {
    val json        = apiGet("/geo/matches/$sellerId") ?: return null
    val matchArray  = json.optJSONArray("matches") ?: return null
    val matches     = mutableListOf<GeoMatch>()
    for (i in 0 until matchArray.length()) {
        val m       = matchArray.getJSONObject(i)
        val rArr    = m.optJSONArray("reasons") ?: JSONArray()
        val reasons = (0 until rArr.length()).map { rArr.getString(it) }
        matches.add(GeoMatch(
            buyerName          = m.optString("buyer_name", "Unknown"),
            industry           = m.optString("industry", ""),
            city               = m.optString("city", ""),
            state              = m.optString("state", ""),
            distanceKm         = m.optDouble("distance_km", 0.0),
            quantitySufficient = m.optBoolean("quantity_sufficient", false),
            quantityRequired   = m.optDouble("quantity_required_tonnes", 0.0),
            reuseApplication   = m.optString("reuse_application", ""),
            reasons            = reasons,
            verificationRequired = m.optBoolean("verification_required", true),
            matchStatus        = m.optString("match_status", "Unknown")
        ))
    }
    val loc = json.optJSONObject("seller_location")
    val locStr = if (loc != null) {
        "%.3f, %.3f".format(loc.optDouble("latitude"), loc.optDouble("longitude"))
    } else ""
    return GeoMatchesResult(
        sellerName    = json.optString("seller_name", sellerId),
        sellerLocation = locStr,
        totalMatches  = json.optInt("total_matches", 0),
        matches       = matches,
        dataType      = json.optString("data_type", "")
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ReLoopApp() }
    }
}

private enum class Screen {
    HOME, DISCOVER, LISTING, REQUIREMENT, MATCHES, DETAILS, PASSPORT, PROFILE
}

// ─────────────────────────────────────────────────────────────────────────────
// ROOT COMPOSABLE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ReLoopApp() {
    var screen         by remember { mutableStateOf(Screen.HOME) }
    var previousScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedItem   by remember { mutableStateOf(demoMaterials.first()) }
    var localMaterials by remember { mutableStateOf(emptyList<MaterialItem>()) }
    var query          by remember { mutableStateOf("") }

    fun openDetails(item: MaterialItem) {
        selectedItem   = item
        previousScreen = screen
        screen         = Screen.DETAILS
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary         = Green,
            onPrimary       = Color.White,
            secondary       = Lime,
            onSecondary     = DarkGreen,
            background      = Page,
            onBackground    = Ink,
            surface         = Color.White,
            onSurface       = Ink,
            surfaceVariant  = GreenLight,
            onSurfaceVariant = Muted,
            outline         = Line,
            error           = ErrorRed,
            onError         = Color.White
        )
    ) {
        val topLevelScreens = listOf(Screen.HOME, Screen.DISCOVER, Screen.LISTING, Screen.PROFILE)

        Scaffold(
            containerColor = Page,
            topBar = {
                AppHeader(
                    onHome    = { screen = Screen.HOME },
                    onProfile = { screen = Screen.PROFILE }
                )
            },
            bottomBar = {
                if (screen in topLevelScreens) {
                    BottomNav(screen) { screen = it }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        localMaterials = localMaterials,
                        query          = query,
                        onQuery        = { query = it },
                        onDiscover     = { screen = Screen.DISCOVER },
                        onList         = { screen = Screen.LISTING },
                        onRequirement  = { screen = Screen.REQUIREMENT },
                        onOpen         = ::openDetails
                    )
                    Screen.DISCOVER -> DiscoverScreen(
                        localMaterials = localMaterials,
                        query          = query,
                        onQuery        = { query = it },
                        onOpen         = ::openDetails
                    )
                    Screen.LISTING -> SellerForm(
                        onBack = { screen = Screen.HOME },
                        onLocalSave = { item ->
                            localMaterials = listOf(item) + localMaterials
                            selectedItem   = item
                            previousScreen = Screen.HOME
                            screen         = Screen.DETAILS
                        }
                    )
                    Screen.REQUIREMENT -> BuyerForm(
                        onBack         = { screen = Screen.HOME },
                        onMatchesReady = { screen = Screen.MATCHES }
                    )
                    Screen.MATCHES -> MatchesScreen(
                        fromMaterial = selectedItem,
                        onBack       = { screen = previousScreen },
                        onPassport   = { screen = Screen.PASSPORT }
                    )
                    Screen.DETAILS -> MaterialDetails(
                        item      = selectedItem,
                        onBack    = { screen = previousScreen },
                        onMatches = {
                            previousScreen = Screen.DETAILS
                            screen         = Screen.MATCHES
                        },
                        onPassport = { screen = Screen.PASSPORT }
                    )
                    Screen.PASSPORT -> PassportScreen(
                        item   = selectedItem,
                        onBack = { screen = Screen.DETAILS }
                    )
                    Screen.PROFILE -> ProfileScreen(
                        onList        = { screen = Screen.LISTING },
                        onRequirement = { screen = Screen.REQUIREMENT }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NAVIGATION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BottomNav(current: Screen, onSelect: (Screen) -> Unit) {
    NavigationBar(
        containerColor  = Color.White,
        tonalElevation  = 0.dp
    ) {
        val navItems = listOf(
            Triple(Screen.HOME,     Icons.Default.Home,      "Home"),
            Triple(Screen.DISCOVER, Icons.Default.Search,    "Discover"),
            Triple(Screen.LISTING,  Icons.Default.Inventory2,"List"),
            Triple(Screen.PROFILE,  Icons.Default.Person,    "Profile")
        )
        navItems.forEach { (s, icon, label) ->
            NavigationBarItem(
                selected  = current == s,
                onClick   = { onSelect(s) },
                icon      = { Icon(icon, null) },
                label     = { Text(label) },
                colors    = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Green,
                    selectedTextColor   = Green,
                    indicatorColor      = GreenLight,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HEADER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AppHeader(onHome: () -> Unit, onProfile: () -> Unit) {
    Surface(
        color           = Color.White,
        shadowElevation = 0.dp,
        border          = BorderStroke(0.5.dp, Line)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f).clickable { onHome() }) {
                Text(
                    "ReLoop",
                    fontSize    = 22.sp,
                    fontWeight  = FontWeight.Bold,
                    color       = Green
                )
                Text(
                    "INDUSTRIAL MATERIAL EXCHANGE",
                    fontSize      = 9.sp,
                    color         = Muted,
                    letterSpacing = 0.9.sp
                )
            }
            IconButton(onClick = onProfile) {
                Icon(Icons.Default.Person, "Profile", tint = Muted)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HOME SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeScreen(
    localMaterials: List<MaterialItem>,
    query: String,
    onQuery: (String) -> Unit,
    onDiscover: () -> Unit,
    onList: () -> Unit,
    onRequirement: () -> Unit,
    onOpen: (MaterialItem) -> Unit
) {
    var activeCategory by remember { mutableStateOf("All") }
    var liveMaterials  by remember { mutableStateOf<List<MaterialItem>>(emptyList()) }
    var loadingLive    by remember { mutableStateOf(true) }
    val categories     = listOf("All", "Mineral", "Foundry", "Metal", "Process")

    LaunchedEffect(Unit) {
        loadingLive   = true
        liveMaterials = fetchLiveMaterials()
        loadingLive   = false
    }

    val allMaterials = liveMaterials + localMaterials + demoMaterials
    val filtered = allMaterials.filter { item ->
        val matchText     = item.name.contains(query, true) || item.category.contains(query, true)
        val matchCategory = activeCategory == "All" || item.category.contains(activeCategory, true)
        matchText && matchCategory
    }

    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero row
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Find materials",
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Ink
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Green, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Punjab · Haryana · Set in profile", fontSize = 12.sp, color = Muted)
                    }
                }
                Surface(
                    color  = Green,
                    shape  = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(42.dp).clickable { onList() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, "List material", tint = Color.White)
                    }
                }
            }
        }

        item { SearchField(query, onQuery) }

        // Action buttons
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick  = onList,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(9.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("List material", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick  = onRequirement,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(9.dp),
                    border   = BorderStroke(1.dp, Green)
                ) {
                    Text("Find material", color = Green, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Category chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Browse by material", fontWeight = FontWeight.SemiBold, color = Ink)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = activeCategory == cat,
                            onClick  = { activeCategory = cat },
                            label    = { Text(cat, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Lime,
                                selectedLabelColor     = DarkGreen,
                                containerColor         = Color.White,
                                labelColor             = Muted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled             = true,
                                selected            = activeCategory == cat,
                                borderColor         = Line,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        // Section header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Available materials",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Ink
                    )
                    if (loadingLive) {
                        Text("Loading from server…", fontSize = 12.sp, color = Muted)
                    } else {
                        val liveCount = liveMaterials.size
                        val label = if (liveCount > 0)
                            "$liveCount live · ${filtered.size} total"
                        else
                            "${filtered.size} sample materials"
                        Text(label, fontSize = 12.sp, color = Muted)
                    }
                }
                TextButton(onClick = onDiscover) {
                    Text("View all", color = Green, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (loadingLive && liveMaterials.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Green, strokeWidth = 2.dp)
                        Text("Connecting to server…", color = Muted, fontSize = 13.sp)
                    }
                }
            }
        }

        if (filtered.isEmpty() && !loadingLive) {
            item {
                EmptyState("No materials found", "Try a different keyword or category.")
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                MaterialRow(item = item, onClick = { onOpen(item) })
            }
        }

        item {
            Text(
                "Live listings via FastAPI · Demo materials labelled · Quantities not verified in real time",
                fontSize = 11.sp,
                color    = Muted
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DISCOVER SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DiscoverScreen(
    localMaterials: List<MaterialItem>,
    query: String,
    onQuery: (String) -> Unit,
    onOpen: (MaterialItem) -> Unit
) {
    var category      by remember { mutableStateOf("All") }
    var liveMaterials by remember { mutableStateOf<List<MaterialItem>>(emptyList()) }
    var loading       by remember { mutableStateOf(true) }
    val categories    = listOf("All", "Mineral", "Foundry", "Metal", "Process")

    LaunchedEffect(Unit) {
        liveMaterials = fetchLiveMaterials()
        loading = false
    }

    val all = liveMaterials + localMaterials + demoMaterials
    val filtered = all.filter {
        val textMatch = it.name.contains(query, true) || it.category.contains(query, true)
        val catMatch  = category == "All" || it.category.contains(category, true)
        textMatch && catMatch
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text("Discover", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(10.dp))
            SearchField(query, onQuery)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick  = { category = cat },
                        label    = { Text(cat, fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Lime,
                            selectedLabelColor     = DarkGreen,
                            containerColor         = Color.White,
                            labelColor             = Muted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = category == cat,
                            borderColor         = Line,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Line)
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green)
            }
        } else if (filtered.isEmpty()) {
            EmptyState("No materials found", "Try another keyword or remove the category filter.")
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    MaterialRow(item = item, onClick = { onOpen(item) })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SELLER FORM  →  POST /materials
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SellerForm(
    onBack: () -> Unit,
    onLocalSave: (MaterialItem) -> Unit
) {
    var sellerName   by remember { mutableStateOf("") }
    var materialName by remember { mutableStateOf("") }
    var industry     by remember { mutableStateOf("Thermal Power Generation") }
    var quantity     by remember { mutableStateOf("") }
    var cityKey      by remember { mutableStateOf(cityCoordinates.keys.first()) }
    var description  by remember { mutableStateOf("") }
    var submitting   by remember { mutableStateOf(false) }
    var resultMsg    by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var error        by remember { mutableStateOf("") }

    val industries   = listOf(
        "Thermal Power Generation", "Steel Manufacturing", "Foundry",
        "Chemical Processing", "Cement Manufacturing", "Textile Industry",
        "Food Processing", "Other"
    )
    val coords       = cityCoordinates[cityKey]!!

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackTitle("List a material", onBack)

        Text(
            "Submit a material your business has available. This creates a live listing via the ReLoop API.",
            color    = Muted,
            fontSize = 13.sp
        )

        InfoBlock(
            "Fly ash listings",
            "Geographic matching is currently enabled for fly ash. Other materials are stored and visible but will not appear in match results until additional pathways are added.",
            isWarning = false
        )

        FieldLabel("Business / seller name")
        FormTextField(sellerName, { sellerName = it }, "e.g. Rajpura Power Station")

        FieldLabel("Material name")
        FormTextField(materialName, { materialName = it }, "e.g. Fly Ash")

        DropdownField("Industry type", industry, industries) { industry = it }

        FieldLabel("Quantity (tonnes)")
        FormTextField(
            quantity, { quantity = it },
            "e.g. 120",
            numeric = true
        )

        DropdownField(
            "Collection city / state",
            cityKey,
            cityCoordinates.keys.toList()
        ) { cityKey = it }

        FieldLabel("Additional notes (optional)")
        FormTextField(
            description, { description = it },
            "Quality notes, constraints, documentation available…",
            singleLine = false
        )

        if (error.isNotEmpty()) {
            Surface(color = ErrorBg, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(error, color = ErrorRed, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
        }

        resultMsg?.let { (success, msg) ->
            Surface(
                color  = if (success) GreenLight else ErrorBg,
                shape  = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                        null,
                        tint = if (success) Green else ErrorRed
                    )
                    Text(msg, color = if (success) Green else ErrorRed, fontSize = 13.sp)
                }
            }
        }

        val qtyDouble = quantity.trim().toDoubleOrNull()

        Button(
            onClick = {
                error = when {
                    sellerName.isBlank()   -> "Enter your business name."
                    materialName.isBlank() -> "Enter the material name."
                    qtyDouble == null || qtyDouble <= 0.0 -> "Enter a valid quantity in tonnes."
                    else -> ""
                }
                if (error.isNotEmpty()) return@Button

                submitting = true
                resultMsg  = null
            },
            enabled  = !submitting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(9.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            if (submitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                if (submitting) "Submitting…" else "Submit listing",
                fontWeight = FontWeight.SemiBold
            )
        }

        // Trigger the API call when submitting
        if (submitting && error.isEmpty()) {
            LaunchedEffect(submitting) {
                val cityParts = cityKey.split(",")
                val body = JSONObject().apply {
                    put("seller_name",          sellerName.trim())
                    put("material_name",        materialName.trim())
                    put("source_industry",      industry)
                    put("quantity_tonnes",      qtyDouble ?: 0.0)
                    put("city",                 cityParts.getOrElse(0) { cityKey }.trim())
                    put("state",                cityParts.getOrElse(1) { "Punjab" }.trim())
                    put("latitude",             coords.first)
                    put("longitude",            coords.second)
                    put("material_properties",  JSONObject())
                    put("available_documents",  JSONArray())
                    put("is_synthetic",         false)
                }
                val (success, msg) = postMaterial(body)
                resultMsg  = Pair(success, msg)
                submitting = false

                if (success) {
                    // Save locally for immediate visibility in this session
                    onLocalSave(
                        MaterialItem(
                            id          = System.currentTimeMillis().toInt(),
                            name        = materialName.trim(),
                            category    = industry,
                            quantity    = "${qtyDouble?.toInt()} tonnes",
                            location    = cityKey,
                            use         = "Industrial reuse",
                            description = description.trim().ifEmpty { "Listed via ReLoop. Verification pending." },
                            isLive      = true
                        )
                    )
                }
            }
        }

        TextButton(
            onClick = {
                error = when {
                    materialName.isBlank() -> "Enter a material name."
                    else -> ""
                }
                if (error.isEmpty()) {
                    onLocalSave(
                        MaterialItem(
                            id          = System.currentTimeMillis().toInt(),
                            name        = materialName.trim().ifEmpty { "Unnamed material" },
                            category    = industry,
                            quantity    = quantity.trim().ifEmpty { "TBD" } + " tonnes",
                            location    = cityKey,
                            use         = "Industrial reuse",
                            description = description.trim().ifEmpty { "Demo listing only — not submitted to server." },
                            isLive      = false
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save as demo (no upload)", color = Muted, fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BUYER FORM  →  POST /requirements
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BuyerForm(
    onBack: () -> Unit,
    onMatchesReady: () -> Unit
) {
    var buyerName    by remember { mutableStateOf("") }
    var buyerInd     by remember { mutableStateOf("Cement Manufacturing") }
    var material     by remember { mutableStateOf("") }
    var quantity     by remember { mutableStateOf("") }
    var cityKey      by remember { mutableStateOf(cityCoordinates.keys.first()) }
    var submitting   by remember { mutableStateOf(false) }
    var resultMsg    by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var error        by remember { mutableStateOf("") }

    val buyerIndustries = listOf(
        "Cement Manufacturing", "Brick Manufacturing", "Concrete Products",
        "Road Construction", "Steel Manufacturing", "Chemical Processing",
        "Other"
    )
    val coords = cityCoordinates[cityKey]!!

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackTitle("Find a material", onBack)
        Text(
            "Describe what your business needs. We'll find nearby suppliers with compatible materials.",
            color = Muted, fontSize = 13.sp
        )

        FieldLabel("Business / buyer name")
        FormTextField(buyerName, { buyerName = it }, "e.g. Patiala Eco Bricks")

        DropdownField("Buyer industry", buyerInd, buyerIndustries) { buyerInd = it }

        FieldLabel("Required material")
        FormTextField(material, { material = it }, "e.g. Fly Ash")

        FieldLabel("Required quantity (tonnes)")
        FormTextField(quantity, { quantity = it }, "e.g. 200", numeric = true)

        DropdownField(
            "Preferred location",
            cityKey,
            cityCoordinates.keys.toList()
        ) { cityKey = it }

        if (error.isNotEmpty()) {
            Surface(color = ErrorBg, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(error, color = ErrorRed, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
        }

        resultMsg?.let { (success, msg) ->
            Surface(
                color    = if (success) GreenLight else ErrorBg,
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                        null, tint = if (success) Green else ErrorRed
                    )
                    Text(msg, color = if (success) Green else ErrorRed, fontSize = 13.sp)
                }
            }
        }

        val qtyDouble = quantity.trim().toDoubleOrNull()

        Button(
            onClick = {
                error = when {
                    buyerName.isBlank()                      -> "Enter your business name."
                    material.isBlank()                       -> "Enter the material you need."
                    qtyDouble == null || qtyDouble <= 0.0   -> "Enter a valid quantity in tonnes."
                    else                                     -> ""
                }
                if (error.isNotEmpty()) return@Button
                submitting = true
                resultMsg  = null
            },
            enabled  = !submitting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(9.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            if (submitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                if (submitting) "Submitting…" else "Find matching materials",
                fontWeight = FontWeight.SemiBold
            )
        }

        if (submitting && error.isEmpty()) {
            LaunchedEffect(submitting) {
                val cityParts = cityKey.split(",")
                val body = JSONObject().apply {
                    put("buyer_name",               buyerName.trim())
                    put("buyer_industry",           buyerInd)
                    put("required_material",        material.trim())
                    put("required_quantity_tonnes", qtyDouble ?: 0.0)
                    put("city",                     cityParts.getOrElse(0) { cityKey }.trim())
                    put("state",                    cityParts.getOrElse(1) { "Punjab" }.trim())
                    put("latitude",                 coords.first)
                    put("longitude",                coords.second)
                    put("required_properties",      JSONObject())
                    put("required_documents",       JSONArray())
                    put("is_synthetic",             false)
                }
                val (success, msg) = postRequirement(body)
                resultMsg  = Pair(success, msg)
                submitting = false
                if (success) onMatchesReady()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MATCHES SCREEN  →  GET /geo/matches/{sellerId}
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MatchesScreen(
    fromMaterial: MaterialItem,
    onBack: () -> Unit,
    onPassport: () -> Unit
) {
    var selectedSellerIndex by remember { mutableStateOf(0) }
    var result              by remember { mutableStateOf<GeoMatchesResult?>(null) }
    var loading             by remember { mutableStateOf(true) }
    var apiError            by remember { mutableStateOf(false) }

    val (sellerId, sellerLabel) = demoSellers[selectedSellerIndex]

    LaunchedEffect(sellerId) {
        loading  = true
        apiError = false
        result   = fetchGeoMatches(sellerId)
        if (result == null) apiError = true
        loading  = false
    }

    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { BackTitle("Match results", onBack) }

        // Seller selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Demo seller", fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    demoSellers.forEachIndexed { index, (_, label) ->
                        FilterChip(
                            selected = selectedSellerIndex == index,
                            onClick  = { selectedSellerIndex = index },
                            label    = { Text(label, fontSize = 11.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Lime,
                                selectedLabelColor     = DarkGreen,
                                containerColor         = Color.White,
                                labelColor             = Muted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled             = true,
                                selected            = selectedSellerIndex == index,
                                borderColor         = Line,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        if (loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = Green)
                        Text("Fetching matches from server…", color = Muted, fontSize = 13.sp)
                    }
                }
            }
        } else if (apiError || result == null) {
            item {
                InfoBlock(
                    "Cannot reach matching API",
                    "Check that the FastAPI server is running and BASE_URL is correct ($BASE_URL). " +
                            "For emulator: use 10.0.2.2:8000. For a physical device: use your machine's local IP.",
                    isWarning = true
                )
            }
        } else {
            val res = result!!

            // Summary header
            item {
                Surface(
                    color  = Green,
                    shape  = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            res.sellerName,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 17.sp
                        )
                        Text(
                            "Material: Fly Ash  ·  Matches found: ${res.totalMatches}",
                            color    = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        if (res.dataType == "synthetic_demo") {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                color  = Lime,
                                shape  = RoundedCornerShape(5.dp)
                            ) {
                                Text(
                                    "SYNTHETIC DEMO DATA",
                                    modifier      = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color         = DarkGreen,
                                    fontSize      = 9.sp,
                                    fontWeight    = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }
                    }
                }
            }

            if (res.matches.isEmpty()) {
                item {
                    EmptyState(
                        "No matches found",
                        "Geographic matching currently returns results for Fly Ash listings within 300 km."
                    )
                }
            } else {
                items(res.matches) { match ->
                    MatchCard(match)
                }
            }

            item {
                InfoBlock(
                    "Verification required",
                    "All matches are preliminary suggestions. Material suitability, quantity confirmation and " +
                            "documentation must be independently verified before any commercial transaction.",
                    isWarning = true
                )
            }
        }

        item {
            OutlinedButton(
                onClick  = onPassport,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(9.dp),
                border   = BorderStroke(1.dp, Green)
            ) {
                Icon(Icons.Default.QrCode2, null, tint = Green)
                Spacer(Modifier.width(8.dp))
                Text("View Material Passport", color = Green)
            }
        }
    }
}

@Composable
private fun MatchCard(match: GeoMatch) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        border    = BorderStroke(1.dp, Line),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Buyer name + status chip
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(match.buyerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                    Text(match.industry, fontSize = 12.sp, color = Muted)
                }
                StatusChip(match.matchStatus, match.quantitySufficient)
            }

            // Distance + location
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOn, null, tint = Green, modifier = Modifier.size(14.dp))
                Text(
                    "${match.city}, ${match.state}  ·  ${match.distanceKm} km away",
                    fontSize = 13.sp,
                    color    = Ink
                )
            }

            HorizontalDivider(color = Line)

            // Reuse application
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("REUSE PATHWAY", fontSize = 9.sp, color = Muted, letterSpacing = 0.5.sp)
                Text(match.reuseApplication, fontSize = 14.sp, color = Green, fontWeight = FontWeight.SemiBold)
            }

            // Quantity fit
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("QUANTITY REQUIRED", fontSize = 9.sp, color = Muted, letterSpacing = 0.5.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        if (match.quantitySufficient) Icons.Default.CheckCircle else Icons.Default.Warning,
                        null,
                        tint     = if (match.quantitySufficient) Green else Amber,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        "${match.quantityRequired.toInt()} tonnes  –  ${if (match.quantitySufficient) "Quantity available" else "Insufficient quantity"}",
                        fontSize = 13.sp,
                        color    = if (match.quantitySufficient) Ink else Amber
                    )
                }
            }

            // Explanation reasons
            Surface(color = Page, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("WHY THIS MATCH", fontSize = 9.sp, color = Muted, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(2.dp))
                    match.reasons.forEach { reason ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("·", color = Green, fontWeight = FontWeight.Bold)
                            Text(reason, fontSize = 12.sp, color = Muted)
                        }
                    }
                }
            }

            if (match.verificationRequired) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.Info, null, tint = Amber, modifier = Modifier.size(13.dp))
                    Text(
                        "Independent verification required before engagement",
                        fontSize = 11.sp,
                        color    = Amber
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String, sufficient: Boolean) {
    val bgColor   = if (sufficient) GreenLight else AmberBg
    val textColor = if (sufficient) Green else Amber

    Surface(color = bgColor, shape = RoundedCornerShape(6.dp)) {
        Text(
            status,
            modifier      = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color         = textColor,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MATERIAL DETAILS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaterialDetails(
    item: MaterialItem,
    onBack: () -> Unit,
    onMatches: () -> Unit,
    onPassport: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackTitle("Material details", onBack)

        // Status badge
        Row {
            Surface(
                color = if (item.isLive) GreenLight else AmberBg,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    if (item.isLive) "LIVE LISTING" else "DEMO DATA",
                    modifier      = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color         = if (item.isLive) Green else Amber,
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Surface(
            color    = Color.White,
            shape    = RoundedCornerShape(12.dp),
            border   = BorderStroke(1.dp, Line),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column {
                    Text(item.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(item.category, color = Muted, fontSize = 13.sp)
                }
                HorizontalDivider(color = Line)
                DetailLine("Available quantity", item.quantity)
                DetailLine("Collection location", item.location)
                DetailLine("Potential reuse", item.use)
                HorizontalDivider(color = Line)
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Material notes", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(item.description, color = Muted, lineHeight = 21.sp, fontSize = 13.sp)
                }
            }
        }

        if (!item.isLive) {
            InfoBlock(
                "Verification status",
                "This is a sample listing. Supplier identity, material quality and documentation are not verified.",
                isWarning = true
            )
        }

        Button(
            onClick  = onMatches,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(9.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text("View geographic matches", fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick  = onPassport,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(9.dp),
            border   = BorderStroke(1.dp, Green)
        ) {
            Icon(Icons.Default.QrCode2, null, tint = Green)
            Spacer(Modifier.width(8.dp))
            Text("Material Passport", color = Green, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PASSPORT SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PassportScreen(item: MaterialItem, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackTitle("Material Passport", onBack)

        Surface(
            color    = Color.White,
            border   = BorderStroke(1.dp, Line),
            shape    = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(color = GreenLight, shape = CircleShape, modifier = Modifier.size(72.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.QrCode2, "Passport", tint = Green, modifier = Modifier.size(40.dp))
                    }
                }
                Text(
                    "MATERIAL PASSPORT",
                    fontWeight    = FontWeight.Bold,
                    color         = Green,
                    letterSpacing = 0.8.sp,
                    fontSize      = 11.sp
                )
                Text(item.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("Local session ID: #${item.id}", color = Muted, fontSize = 12.sp)
            }
        }

        Surface(
            color    = Color.White,
            border   = BorderStroke(1.dp, Line),
            shape    = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Material metadata", fontWeight = FontWeight.SemiBold, color = Ink)
                HorizontalDivider(color = Line)
                DetailLine("Category",           item.category)
                DetailLine("Quantity available",  item.quantity)
                DetailLine("Source location",     item.location)
                DetailLine("Intended reuse",      item.use)
                DetailLine("Data status",         if (item.isLive) "Live — from ReLoop API" else "Demo — local session")
            }
        }

        InfoBlock(
            "QR code generation",
            "A real, scannable QR code is generated from the stable passport identifier returned by the backend. " +
                    "This feature requires the Material Passport API to be connected and return a passport ID.",
            isWarning = false
        )

        InfoBlock(
            "Traceability",
            "Production passports link to supply chain events, quality documents and chain-of-custody records. " +
                    "These fields are available once the Passport API is integrated.",
            isWarning = false
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PROFILE SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileScreen(onList: () -> Unit, onRequirement: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)

        Surface(
            color    = GreenLight,
            shape    = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = Green, shape = CircleShape, modifier = Modifier.size(52.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Column {
                    Text("Business account", fontWeight = FontWeight.Bold, color = Ink)
                    Text("Profile setup via API pending", color = Muted, fontSize = 12.sp)
                }
            }
        }

        InfoBlock(
            "Business details",
            "Business name, registration, address and contact details are displayed here once account/profile APIs are connected.",
            isWarning = false
        )

        Text("Quick actions", fontWeight = FontWeight.SemiBold, color = Ink)

        Button(
            onClick  = onList,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(9.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Create a material listing", fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick  = onRequirement,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(9.dp),
            border   = BorderStroke(1.dp, Green)
        ) {
            Text("Submit a material requirement", color = Green, fontWeight = FontWeight.SemiBold)
        }

        Surface(color = GreenLight, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Connected to", fontSize = 10.sp, color = Muted, letterSpacing = 0.4.sp)
                Text("reloop-ui.onrender.com", fontSize = 12.sp, color = Green, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = { Text("Search materials, categories…", fontSize = 14.sp) },
        leadingIcon   = { Icon(Icons.Default.Search, null, tint = Muted) },
        trailingIcon  = {
            if (value.isNotEmpty()) {
                TextButton(onClick = { onValueChange("") }) {
                    Text("Clear", color = Muted, fontSize = 12.sp)
                }
            }
        },
        singleLine = true,
        shape      = RoundedCornerShape(10.dp),
        colors     = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor   = Color.White,
            unfocusedBorderColor    = Line,
            focusedBorderColor      = Green
        )
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    numeric: Boolean   = false,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = { Text(placeholder, fontSize = 13.sp, color = Muted) },
        singleLine    = singleLine,
        maxLines      = if (singleLine) 1 else 3,
        keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
        shape  = RoundedCornerShape(9.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor   = Color.White,
            unfocusedBorderColor    = Line,
            focusedBorderColor      = Green
        )
    )
}

@Composable
private fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        FieldLabel(label)
        Box {
            OutlinedButton(
                onClick   = { expanded = true },
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(9.dp),
                border    = BorderStroke(1.dp, Line),
                colors    = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text(
                    selected,
                    modifier = Modifier.weight(1f),
                    color    = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start
                )
                Text("▾", color = Muted)
            }
            DropdownMenu(
                expanded          = expanded,
                onDismissRequest  = { expanded = false }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text    = { Text(opt, fontSize = 14.sp) },
                        onClick = { onSelect(opt); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(label: String) {
    Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink)
}

@Composable
private fun BackTitle(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "Go back", tint = Ink)
        }
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = Muted, modifier = Modifier.width(140.dp), fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Ink, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun InfoBlock(title: String, description: String, isWarning: Boolean) {
    Surface(
        color    = if (isWarning) AmberBg else Color.White,
        border   = BorderStroke(1.dp, if (isWarning) Amber.copy(alpha = 0.3f) else Line),
        shape    = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                if (isWarning) Icons.Default.Warning else Icons.Default.Info,
                null,
                tint     = if (isWarning) Amber else Muted,
                modifier = Modifier.size(16.dp).padding(top = 2.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (isWarning) Amber else Ink)
                Text(description, color = if (isWarning) Amber else Muted, lineHeight = 20.sp, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MaterialRow(item: MaterialItem, onClick: () -> Unit) {
    val symbol = when {
        item.name.contains("Fly", true) || item.category.contains("Mineral", true) -> "FA"
        item.category.contains("Metal", true)   -> "MS"
        item.category.contains("Foundry", true) -> "FS"
        item.category.contains("Process", true) -> "PW"
        else -> item.name.take(2).uppercase()
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        border    = BorderStroke(1.dp, Line),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    color  = GreenLight,
                    shape  = RoundedCornerShape(9.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(symbol, color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(item.category, fontSize = 12.sp, color = Muted)
                }
                Surface(
                    color  = if (item.isLive) GreenLight else AmberBg,
                    shape  = RoundedCornerShape(5.dp)
                ) {
                    Text(
                        if (item.isLive) "LIVE" else "DEMO",
                        modifier      = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        color         = if (item.isLive) Green else Amber,
                        fontSize      = 9.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            HorizontalDivider(color = Line)

            Row {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("QUANTITY", fontSize = 9.sp, color = Muted, letterSpacing = 0.4.sp)
                    Text(item.quantity, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("COLLECTION AREA", fontSize = 9.sp, color = Muted, letterSpacing = 0.4.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Green, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(item.location, fontSize = 12.sp, color = Ink)
                    }
                }
            }

            Surface(color = Page, shape = RoundedCornerShape(8.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("POTENTIAL REUSE", fontSize = 9.sp, color = Muted, letterSpacing = 0.4.sp)
                        Text(item.use, fontSize = 12.sp, color = Green, fontWeight = FontWeight.Medium)
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = Green, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, description: String) {
    Column(
        Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Search, null, tint = Line, modifier = Modifier.size(40.dp))
        Text(title, fontWeight = FontWeight.SemiBold, color = Ink)
        Text(description, color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

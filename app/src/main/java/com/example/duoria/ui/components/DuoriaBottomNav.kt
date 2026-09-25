package com.example.duoria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duoria.ui.theme.*

enum class AppDestination(
    val route: String,
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
) {
    HOME("home", "Hogar", Icons.Filled.Home, Icons.Outlined.Home),
    CONEXION("conexion", "Conexión", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    CINE("cine", "Cine", Icons.Filled.Movie, Icons.Outlined.Movie),
    FEED("feed", "Feed", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary),
    BOVEDA("boveda", "Bóveda", Icons.Filled.Lock, Icons.Outlined.Lock)
}

@Composable
fun DuoriaBottomNav(
    currentRoute: String,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestination.entries.forEach { destination ->
                val isSelected = currentRoute == destination.route
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigate(destination) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_tab_${destination.route}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) destination.iconFilled else destination.iconOutlined,
                        contentDescription = destination.label,
                        tint = if (isSelected) RosePrimary else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = destination.label,
                        style = Typography.labelSmall.copy(
                            color = if (isSelected) RosePrimary else TextMuted,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}

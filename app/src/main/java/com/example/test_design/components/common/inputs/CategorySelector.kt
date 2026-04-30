package com.example.test_design.components.common.inputs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import com.example.test_design.views.ui.theme.AppMotion
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.test_design.R
import com.example.test_design.utils.CategoryMapper
import androidx.compose.foundation.background
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Surface

const val CATEGORY_FAVORITES = "favorites_id"
const val CATEGORY_ALL = "all_id"
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(
    categories: List<String>,
    selected: Set<String>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onSelect: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    val alignment = if (isTablet) Alignment.Start else Alignment.CenterHorizontally
    val horizontalPadding = if (isTablet) 16.dp else 0.dp

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(top = 6.dp)
        .padding(horizontal = horizontalPadding),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .then(
                    if (isTablet) {
                        Modifier.width(500.dp).padding(horizontal = 0.dp)
                    } else {
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    }
                )
                .clip(RoundedCornerShape(16.dp))
                .background(if (isExpanded) Color(0xFFF7F7F7) else Color.Transparent)
                .clickable (
                    indication = null, // tar bort ripple
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { onExpandChange(!isExpanded) }
                .border(
                    width = if (!isExpanded) 1.dp else 0.dp,
                    color = if (isExpanded) Color.Transparent else Color.Black.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.category_title),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.weight(1f))

            val displayText = when {
                selected.contains(CATEGORY_ALL) -> stringResource(R.string.category_all)
                selected.contains(CATEGORY_FAVORITES) -> stringResource(R.string.category_favorites)
                selected.size == 1 -> CategoryMapper.getDisplayName(selected.first()) // Använd mappen
                else -> stringResource(R.string.category_selected_count, selected.size)
            }

            Text(
                text = displayText,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.padding(horizontal = 8.dp)) //lite mellanrum före knappen

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = AppMotion.ExpandEnter,
            exit = AppMotion.CollapseExit
        ) {

            FlowRow(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .then(
                        if (isTablet) {
                            Modifier.width(500.dp)
                        } else {
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        }
                    )
                    .padding(top = 12.dp, bottom = 12.dp)
            ) {
                categories.forEach { categoryId ->
                    CategoryChip(
                        title = if (categoryId == CATEGORY_ALL) stringResource(R.string.category_all)
                        else CategoryMapper.getDisplayName(categoryId),
                        isSelected = selected.contains(categoryId),
                        isFavoriteIcon = categoryId == CATEGORY_FAVORITES,
                        onClick = {
                            val newSet = when {
                                categoryId == CATEGORY_ALL -> setOf(CATEGORY_ALL)
                                selected.contains(categoryId) -> {
                                    val result = selected - categoryId
                                    result.ifEmpty { setOf(CATEGORY_ALL) }
                                }
                                else -> (selected - CATEGORY_ALL) + categoryId
                            }
                            onSelect(newSet)
                        }
                    )
                }
        }
    }
}
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    isFavoriteIcon: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.Black else Color(0xFFF7F7F7),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(0.03f)) else null,
        modifier = Modifier.height(44.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = if (isFavoriteIcon) 12.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isFavoriteIcon) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFFFFD500) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color.Black,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DefaultAccent = Color(0xFFC9FF3E)
private val UnselectedBg = Color(0xFF1A1A1A)
private val UnselectedBorder = Color(0xFF2A2A2A)
private val UnselectedText = Color(0xFFE6E6E6)

@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = DefaultAccent
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) Modifier.background(accentColor)
                        else Modifier
                            .background(UnselectedBg)
                            .border(1.dp, UnselectedBorder, RoundedCornerShape(50))
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) Color.Black else UnselectedText
                )
            }
        }
    }
}

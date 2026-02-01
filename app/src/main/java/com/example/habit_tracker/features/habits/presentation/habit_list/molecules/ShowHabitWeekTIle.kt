//package com.example.habit_tracker.features.habits.presentation.habit_list.molecules
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import com.example.habit_tracker.features.habits.HabitUtil.getLast7Days
//import java.time.LocalDate
//
//@Composable
//fun Last7DaysHabitTile(
//    habitName: String,
//    completedDates: Set<LocalDate>
//) {
//    val days = remember { getLast7Days(completedDates) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(
//                color = Color(0xFF121212),
//                shape = RoundedCornerShape(20.dp)
//            )
//            .padding(12.dp)
//    ) {
//
//        // Header row (Last 7 days + weekdays)
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Last 7 days",
//                color = Color.White,
//                style = AppTheme.textStyles.labelSmall,
//                modifier = Modifier.weight(1f)
//            )
//
//            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                days.forEach { day ->
//                    Text(
//                        text = day.date.dayOfWeek.name.take(2)
//                            .lowercase()
//                            .replaceFirstChar { it.uppercase() },
//                        color = if (day.date == LocalDate.now())
//                            Color.White else Color.Gray,
//                        style = AppTheme.textStyles.labelSmall
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // Main tile row
//        Row(
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//
//            // Left icon
//            Box(
//                modifier = Modifier
//                    .size(44.dp)
//                    .background(
//                        color = Color(0xFF1E1E1E),
//                        shape = RoundedCornerShape(14.dp)
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("⚡") // replace with Icon()
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            // Habit name
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .height(44.dp)
//                    .background(
//                        color = Color(0xFF1A0F0F),
//                        shape = RoundedCornerShape(14.dp)
//                    )
//                    .padding(horizontal = 16.dp),
//                contentAlignment = Alignment.CenterStart
//            ) {
//                Text(
//                    text = habitName,
//                    color = Color.White,
//                    style = AppTheme.textStyles.bodySmall
//                )
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            // 7-day boxes
//            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//                days.forEach { day ->
//                    DayMiniBox(
//                        isCompleted = day.isCompleted,
//                        isToday = day.date == LocalDate.now()
//                    )
//                }
//            }
//        }
//    }
//}

package io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gaurav95998030.habittracker.commons.ui.HSpace
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfChip
import io.github.gaurav95998030.habittracker.commons.ui.HfIconButton
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.commons.ui.PullToRefresh
import io.github.gaurav95998030.habittracker.commons.ui.RingStat
import io.github.gaurav95998030.habittracker.commons.ui.accentWash
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.habits.presentation.components.HabitTile
import io.github.gaurav95998030.habittracker.features.habits.presentation.components.HabitTileShimmer
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms.AddHabitSheet
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms.HabitDetailSheet
import io.github.gaurav95998030.habittracker.features.habits.presentation.habit_list.organisms.HabitFormActions
import io.github.gaurav95998030.habittracker.features.habits.presentation.states.ALL_CATEGORIES
import io.github.gaurav95998030.habittracker.features.habits.presentation.viewmodel.HabitViewModel
import io.github.gaurav95998030.habittracker.features.reminders.presentation.NotificationCenterBuilder
import io.github.gaurav95998030.habittracker.features.motivation.presentation.StoryItem
import io.github.gaurav95998030.habittracker.features.motivation.presentation.StackCard
import io.github.gaurav95998030.habittracker.features.motivation.presentation.StackEditorSheet
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.HabitStack
import io.github.gaurav95998030.habittracker.features.motivation.domain.model.StackStep
import io.github.gaurav95998030.habittracker.commons.ui.HfBottomSheet
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import androidx.compose.foundation.layout.navigationBarsPadding
import io.github.gaurav95998030.habittracker.features.motivation.presentation.StoryStrip
import io.github.gaurav95998030.habittracker.features.motivation.presentation.storyItems
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.glow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * Home · Today. Progress ring, category filter, habit tiles; the FAB opens the
 * add sheet, tapping a tile opens its detail sheet.
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitListScreen(
    viewModel: HabitViewModel,
    userName: String,
    focusCategories: Set<String>,
    seenStories: Set<String>,
    onOpenStory: (StoryItem) -> Unit,
    stacks: List<HabitStack>,
    onOpenStack: (String) -> Unit,
    /** Saves a new stack and returns its id. */
    onCreateStack: (name: String, steps: List<StackStep>) -> String,
    onOpenPlans: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val c = AppTheme.colors
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val habits = state.habits.orEmpty()
    val form = viewModel.formState
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showCreateMenu by rememberSaveable { mutableStateOf(false) }
    var showStackEditor by rememberSaveable { mutableStateOf(false) }
    var detailHabitId by rememberSaveable { mutableStateOf<String?>(null) }
    val detailHabit = habits.firstOrNull { it.id == detailHabitId }

    // Notification tap → open that habit's detail once it's in the list.
    LaunchedEffect(state.pendingDetailHabitId, habits) {
        val id = state.pendingDetailHabitId ?: return@LaunchedEffect
        if (habits.any { it.id == id }) {
            detailHabitId = id
            viewModel.consumePendingDetail()
        }
    }

    // "Why you miss" → change a habit's reminder: open its edit sheet here.
    LaunchedEffect(state.pendingEditHabitId, habits) {
        val id = state.pendingEditHabitId ?: return@LaunchedEffect
        habits.firstOrNull { it.id == id }?.let { h ->
            viewModel.startEditingHabit(h)
            showAddSheet = true
            viewModel.consumePendingEdit()
        }
    }

    val onDelete: (Habit) -> Unit = { habit ->
        detailHabitId = null
        viewModel.deleteHabit(habit)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val autoDismiss = launch { delay(3000); snackbar.currentSnackbarData?.dismiss() }
            val result = snackbar.showSnackbar(
                message = "\"${habit.name}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Indefinite,
            )
            autoDismiss.cancel()
            if (result == SnackbarResult.ActionPerformed) viewModel.restoreHabit(habit)
        }
    }

    val today = LocalDate.now()
    val active = habits.filter { it.isActive }
    val doneCount = active.count { HabitStats.isDoneOn(it, today) }
    val categories = remember(habits) { listOf(ALL_CATEGORIES) + habits.map { it.category }.filter { it.isNotBlank() }.distinct() }
    val filter = state.selectedCategory.takeIf { it in categories } ?: ALL_CATEGORIES
    val shown = habits.filter { filter == ALL_CATEGORIES || it.category == filter }
    val pending = remember(habits) { NotificationCenterBuilder.pendingCount(habits) }

    Box(Modifier.fillMaxSize().background(c.background)) {
        PullToRefresh(onRefresh = { viewModel.refreshHabits() }, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize().statusBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "header") {
                    HomeHeader(
                        userName = userName,
                        unread = pending,
                        onOpenNotifications = onOpenNotifications,
                        onOpenProfile = onOpenSettings,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                if (!state.isLoading) {
                    val stories = storyItems(habits)
                    if (stories.isNotEmpty()) {
                        item(key = "stories") {
                            StoryStrip(stories, seenStories, onOpenStory, Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
                if (!state.isLoading && active.isNotEmpty()) {
                    item(key = "progress") {
                        ProgressCard(done = doneCount, total = active.size, modifier = Modifier.padding(bottom = 10.dp))
                    }
                }
                if (!state.isLoading && habits.isNotEmpty()) {
                    item(key = "filters") {
                        LazyRow(
                            Modifier.padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(categories, key = { it }) { cat ->
                                HfChip(label = cat, active = filter == cat, onClick = { viewModel.onCategorySelected(cat) })
                            }
                        }
                    }
                }
                if (!state.isLoading && filter == ALL_CATEGORIES && stacks.isNotEmpty()) {
                    items(stacks, key = { "stack-${it.id}" }) { st ->
                        StackCard(st, habits, onOpen = { onOpenStack(st.id) })
                    }
                }
                when {
                    state.isLoading -> items(4, key = { "shimmer$it" }) { HabitTileShimmer() }
                    habits.isEmpty() -> item(key = "empty") {
                        EmptyState(onAdd = { viewModel.resetForm(); showAddSheet = true }, onBrowsePlans = onOpenPlans)
                    }
                    shown.isEmpty() -> item(key = "emptyFilter") {
                        Text(
                            "No habits in $filter.",
                            style = AppTheme.textStyles.bodyMedium,
                            color = c.textTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp),
                        )
                    }
                    else -> items(shown, key = { it.id }) { habit ->
                        HabitTile(
                            habit = habit,
                            onToggleToday = { viewModel.toggleToday(habit.id) },
                            onOpen = { detailHabitId = habit.id },
                            onDelete = { onDelete(habit) },
                            onResume = { viewModel.setHabitActive(habit, true) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }

        AddFab(
            onClick = { showCreateMenu = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 16.dp),
        )

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = if (c.isDark) c.surface2 else c.textPrimary,
                contentColor = if (c.isDark) c.textPrimary else c.background,
                actionColor = c.primary,
                shape = RoundedCornerShape(16.dp),
            )
        }
    }

    if (showCreateMenu) {
        CreateMenuSheet(
            onNewHabit = { showCreateMenu = false; viewModel.resetForm(); showAddSheet = true },
            onNewStack = { showCreateMenu = false; showStackEditor = true },
            onPlans = { showCreateMenu = false; onOpenPlans() },
            onDismiss = { showCreateMenu = false },
        )
    }

    if (showStackEditor) {
        StackEditorSheet(
            initial = null,
            habits = habits,
            onSave = { name, steps ->
                showStackEditor = false
                onOpenStack(onCreateStack(name, steps))
            },
            onDelete = null,
            onDismiss = { showStackEditor = false },
        )
    }

    if (showAddSheet) {
        AddHabitSheet(
            form = form,
            focusCategories = focusCategories,
            isSaving = state.isAddingHabit == true,
            actions = HabitFormActions(
                onNameChanged = viewModel::onNameChanged,
                onDescriptionChanged = viewModel::onDescriptionChanged,
                onCategoryChanged = viewModel::onCategoryChanged,
                onColorSelected = viewModel::onColorSelected,
                onReminderEnabledChanged = viewModel::onReminderEnabledChanged,
                onReminderTimeSelected = viewModel::onReminderTimeSelected,
                onSave = { viewModel.saveHabit { showAddSheet = false } },
            ),
            onDismiss = { showAddSheet = false; viewModel.resetForm() },
        )
    }

    if (detailHabit != null) {
        HabitDetailSheet(
            habit = detailHabit,
            onDismiss = { detailHabitId = null },
            onToggleDay = { viewModel.toggleDay(detailHabit.id, it.toString()) },
            onEdit = {
                detailHabitId = null
                viewModel.startEditingHabit(detailHabit)
                showAddSheet = true
            },
            onDelete = { onDelete(detailHabit) },
            onSetActive = { viewModel.setHabitActive(detailHabit, it) },
            onStartStreak = { viewModel.toggleToday(detailHabit.id) },
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun greeting(): String {
    val h = LocalTime.now().hour
    return when {
        h < 12 -> "Good morning"
        h < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeHeader(
    userName: String,
    unread: Int,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val initial = userName.trim().firstOrNull()?.uppercaseChar()?.toString()
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                if (userName.isNotBlank()) "${greeting()}, ${userName.trim()}" else greeting(),
                style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp, letterSpacing = 0.2.sp),
                color = c.textSecondary,
            )
            Text(
                "Today",
                style = AppTheme.textStyles.titleLarge.copy(fontSize = 26.sp, letterSpacing = (-0.6).sp),
                color = c.textPrimary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        HfIconButton(
            glyph = PhIcons.Bell,
            onClick = onOpenNotifications,
            size = 44.dp,
            iconSize = 20.dp,
            contentDescription = if (unread > 0) "Notifications, $unread due" else "Notifications",
            badge = if (unread > 0) {
                {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 5.dp)
                            .glow(c.error.copy(alpha = 0.66f), 8.dp, 999.dp, enabled = c.isDark)
                            .clip(CircleShape)
                            .background(c.background)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(c.error)
                            .widthIn(min = 16.dp)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(unread.toString(), style = AppTheme.textStyles.caption.copy(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold), color = c.onError)
                    }
                }
            } else null,
        )
        HSpace(10.dp)
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(c.glassFill)
                .border(1.dp, c.glassBorder, CircleShape)
                .clickable(onClickLabel = "Open settings", onClick = onOpenProfile),
            contentAlignment = Alignment.Center,
        ) {
            if (initial != null) {
                Text(initial, style = AppTheme.textStyles.titleSmall.copy(fontSize = 16.sp), fontWeight = FontWeight.SemiBold, color = c.primary)
            } else {
                PhIcon(PhIcons.User, size = 20.dp, tint = c.primary, weight = PhWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProgressCard(done: Int, total: Int, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val pct = if (total == 0) 0 else (done * 100 / total)
    val left = total - done
    val shape = RoundedCornerShape(AppShapes.card)
    Row(
        modifier
            .fillMaxWidth()
            .glow(c.primary.copy(alpha = 0.08f), 30.dp, AppShapes.card, enabled = c.isDark)
            .clip(shape)
            .background(accentWash(c.primary, 0.1f))
            .border(1.dp, if (c.isDark) c.primary.copy(alpha = 0.2f) else c.glassBorder, shape)
            .padding(18.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RingStat(fraction = done.toFloat() / total.coerceAtLeast(1), value = "$done/$total", size = 76.dp)
        HSpace(18.dp)
        Column(Modifier.weight(1f)) {
            Text(
                when {
                    pct == 100 -> "All done — nice."
                    pct >= 50 -> "Halfway there"
                    else -> "Let's get moving"
                },
                style = AppTheme.textStyles.titleMedium.copy(fontSize = 19.sp, letterSpacing = (-0.3).sp),
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary,
            )
            Text(
                if (pct == 100) "Every habit checked off today."
                else "$left ${if (left == 1) "habit" else "habits"} left to keep your streaks alive.",
                style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp),
                color = c.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit, onBrowsePlans: () -> Unit) {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 54.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(96.dp)
                .glow(c.primary.copy(alpha = 0.13f), 40.dp, 28.dp, enabled = c.isDark)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    if (c.isDark) Brush.radialGradient(listOf(c.primary.copy(alpha = 0.2f), c.surface))
                    else Brush.linearGradient(listOf(c.surface, c.surface))
                )
                .border(1.dp, c.glassBorder, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            PhIcon(PhIcons.SealCheck, size = 48.dp, tint = c.primary, weight = PhWeight.Fill)
        }
        Text(
            "Start your first habit",
            style = AppTheme.textStyles.titleLarge.copy(fontSize = 22.sp, letterSpacing = (-0.3).sp),
            color = c.textPrimary,
            modifier = Modifier.padding(top = 22.dp),
        )
        Text(
            "Small, daily, repeatable. Pick one thing and light up a streak you won't want to break.",
            style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp),
            color = c.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp).widthIn(max = 250.dp),
        )
        HfButton("Add a habit", onClick = onAdd, icon = PhIcons.Plus, modifier = Modifier.padding(top = 24.dp))
        HfButton("Browse starter plans", onClick = onBrowsePlans, variant = HfButtonVariant.Ghost, icon = PhIcons.RocketLaunch,
            modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun AddFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .size(60.dp)
            .glow(c.primary.copy(alpha = if (c.isDark) 0.53f else 0.4f), 30.dp, 20.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(c.primary, c.primaryVariant)))
            .clickable(role = Role.Button, onClickLabel = "Add habit", onClick = onClick)
            .semantics { contentDescription = "Add habit" },
        contentAlignment = Alignment.Center,
    ) {
        PhIcon(PhIcons.Plus, size = 28.dp, tint = c.onPrimary, weight = PhWeight.Bold)
    }
}

/** The add button's chooser: one habit, a stack of habits, or a starter plan. */
@Composable
private fun CreateMenuSheet(onNewHabit: () -> Unit, onNewStack: () -> Unit, onPlans: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    HfBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 22.dp)) {
            Text("Add", style = AppTheme.textStyles.titleLarge.copy(fontSize = 21.sp), color = c.textPrimary, modifier = Modifier.padding(bottom = 14.dp))
            listOf(
                Triple(PhIcons.Plus, "New habit" to "One thing, every day", onNewHabit),
                Triple(PhIcons.Stack, "Habit stack" to "Chain habits so each one unlocks the next", onNewStack),
                Triple(PhIcons.RocketLaunch, "Starter plan" to "A 21- or 30-day program, added in one tap", onPlans),
            ).forEach { (icon, text, action) ->
                val shape = RoundedCornerShape(AppShapes.tile)
                Row(
                    Modifier.padding(bottom = 10.dp).fillMaxWidth().clip(shape).background(c.surface).border(1.dp, c.glassBorder, shape)
                        .clickable(onClick = action).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(c.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        PhIcon(icon, size = 21.dp, tint = c.primary, weight = PhWeight.Bold)
                    }
                    HSpace(13.dp)
                    Column(Modifier.weight(1f)) {
                        Text(text.first, style = AppTheme.textStyles.titleSmall.copy(fontSize = 16.sp), color = c.textPrimary)
                        Text(text.second, style = AppTheme.textStyles.bodySmall, color = c.textTertiary, modifier = Modifier.padding(top = 2.dp))
                    }
                    PhIcon(PhIcons.CaretRight, size = 18.dp, tint = c.textTertiary)
                }
            }
        }
    }
}

package io.github.gaurav95998030.habittracker.features.habits.data.remote.firestore

import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HabitRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : HabitRemoteDataSource {

    private val habits = firestore.collection("habits")

    override suspend fun createHabit(habit: Habit) {
//        val doc = habits.document()
//        val habitWithId = habit.copy(id = doc.id)
//        doc.set(habitWithId).await()
    }

    override suspend fun getHabits(): List<Habit> {
        val snapshot = habits.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Habit::class.java) }
    }

    override suspend fun updateHabit(habit: Habit) {
//        require(habit.id.isNotEmpty()) { "Habit id cannot be empty" }
//        habits.document(habit.id)
//            .set(habit)
//            .await()
    }

    override suspend fun deleteHabit(id: String) {
        habits.document(id)
            .delete()
            .await()
    }
}

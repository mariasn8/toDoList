package com.example.todolist

import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.example.todolist.DB.Task
import com.example.todolist.DB.TaskDAO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ToDoViewModel(private val dao: TaskDAO) : ViewModel() {

    // Holds the current search text. Default is empty.
    private val _searchQuery = MutableStateFlow("")

    // This is the main stream of data the UI observes.
    // It automatically switches between "All Tasks" and "Search Results"
    // whenever _searchQuery changes.
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: LiveData<List<Task>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                dao.getAllTasks() // Returns list sorted by due time 
            } else {
                dao.searchTasks(query) // Returns filtered list 
            }
        }
        .asLiveData()

    // Function to update the search query from the UI
    fun search(query: String) {
        _searchQuery.value = query
    }

    // Requirements: Create, Delete, Edit tasks
    fun addTask(task: Task) {
        viewModelScope.launch {
            dao.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            dao.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }
}

// Boilerplate Factory to allow passing the DAO into the ViewModel
class ToDoViewModelFactory(private val dao: TaskDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ToDoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ToDoViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
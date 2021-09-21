package rs.android.task4.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import rs.android.task4.COLUMN_NAME
import rs.android.task4.DATABASE_SOURCE_NAME_ROOM
import rs.android.task4.locator.locateLazy
import rs.android.task4.repository.Repository
import rs.android.task4.repository.Animal

class AnimalsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repository by locateLazy()
    private var sortingColumn = COLUMN_NAME

    private var _animalsFlow = MutableStateFlow<List<Animal>>(emptyList())
    val animalsFlow: SharedFlow<List<Animal>> = _animalsFlow.asStateFlow()

    private var _sourceDbFlow = MutableStateFlow(DATABASE_SOURCE_NAME_ROOM)
    val sourceDbFlow: SharedFlow<String> = _sourceDbFlow.asStateFlow()

    init {
        emitAllSortBy(sortingColumn)
    }

    fun changeDatabaseSource(nameSource: String) {
        repository.changeSource(nameSource)
        emitAllSortBy(sortingColumn)
    }

    fun setSortBy(sortColumn: String) {
        sortingColumn = sortColumn
        emitAllSortBy(sortingColumn)
    }

    fun emitAllSortBy(sortingColumn: String) {
        repository.getAllSortBy(sortingColumn).onEach { _animalsFlow.emit(it) }
            .launchIn(viewModelScope)
    }

    fun setDbSource(dbSource: String) {
        _sourceDbFlow.value = dbSource
    }
}
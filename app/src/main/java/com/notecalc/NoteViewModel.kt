package com.notecalc

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

// --- Repository ---
class NoteRepository(private val dao: NoteDao) {
    val allNotes: LiveData<List<Note>> = dao.getAllNotes()
    fun getNoteById(id: Int) = dao.getNoteById(id)
    suspend fun insert(note: Note) = dao.insert(note)
    suspend fun update(note: Note) = dao.update(note)
    suspend fun delete(note: Note) = dao.delete(note)
}

// --- ViewModel ---
class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: NoteRepository

    init {
        val db = NoteDatabase.getDatabase(application)
        repo = NoteRepository(db.noteDao())
    }

    val allNotes: LiveData<List<Note>> = repo.allNotes

    fun getNoteById(id: Int) = repo.getNoteById(id)

    fun insert(note: Note) = viewModelScope.launch { repo.insert(note) }
    fun update(note: Note) = viewModelScope.launch { repo.update(note) }
    fun delete(note: Note) = viewModelScope.launch { repo.delete(note) }
}

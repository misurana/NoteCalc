package com.notecalc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.notecalc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter
    private var allNotes: List<Note> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        adapter = NoteAdapter(
            onNoteClick = { note ->
                startActivity(
                    Intent(this, NoteEditorActivity::class.java)
                        .putExtra("NOTE_ID", note.id)
                )
            },
            onNoteDelete = { note ->
                showDeleteDialog(listOf(note.id), "\"${note.title}\"")
            },
            onNoteRename = { note -> showRenameDialog(note) },
            onLongPress = { note ->
                // Enter select mode and select the held note
                adapter.enterSelectMode(note.id)
                binding.toolbarNormal.visibility = View.GONE
                binding.toolbarSelect.visibility = View.VISIBLE
                updateSelectBar(1)
                // Brief toast hint
                Toast.makeText(this, "Tap notes to select · press Back to exit", Toast.LENGTH_SHORT).show()
            },
            onSelectionChanged = { count -> updateSelectBar(count) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.allNotes.observe(this) { notes ->
            allNotes = notes
            adapter.submitList(notes)
            binding.emptyState.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabNewNote.setOnClickListener {
            startActivity(Intent(this, NoteEditorActivity::class.java))
        }

        // Select all button
        binding.btnSelectAll.setOnClickListener {
            adapter.selectAll(allNotes)
        }

        // Delete selected
        binding.btnDeleteSelected.setOnClickListener {
            val ids = adapter.getSelectedIds()
            if (ids.isNotEmpty())
                showDeleteDialog(ids.toList(), "${ids.size} note${if (ids.size > 1) "s" else ""}")
        }
    }

    private fun updateSelectBar(count: Int) {
        binding.tvSelectedCount.text = if (count == 0) "0 selected" else "$count selected"
        binding.btnDeleteSelected.isEnabled = count > 0
        binding.btnDeleteSelected.alpha = if (count > 0) 1f else 0.4f

        // Auto-exit if nothing remains selected
        if (count == 0 && adapter.isSelectMode) exitSelectMode()
    }

    private fun exitSelectMode() {
        adapter.exitSelectMode()
        binding.toolbarNormal.visibility = View.VISIBLE
        binding.toolbarSelect.visibility = View.GONE
    }

    private fun showDeleteDialog(ids: List<Int>, label: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete $label?")
            .setMessage("This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                ids.forEach { id ->
                    allNotes.find { it.id == id }?.let { viewModel.delete(it) }
                }
                exitSelectMode()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRenameDialog(note: Note) {
        val input = EditText(this).apply {
            setText(note.title)
            setSingleLine()
            setPadding(48, 32, 48, 16)
        }
        AlertDialog.Builder(this)
            .setTitle("Rename note")
            .setView(input)
            .setPositiveButton("Rename") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) viewModel.update(note.copy(title = newTitle))
            }
            .setNegativeButton("Cancel", null)
            .show()
        input.requestFocus()
    }

    // Back press exits select mode instead of closing the app
    override fun onBackPressed() {
        if (adapter.isSelectMode) exitSelectMode()
        else super.onBackPressed()
    }
}

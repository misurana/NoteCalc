package com.notecalc

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notecalc.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteDelete: (Note) -> Unit,
    private val onNoteRename: (Note) -> Unit,
    private val onLongPress: (Note) -> Unit,
    private val onSelectionChanged: (Int) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DiffCallback) {

    var isSelectMode = false
        private set
    private val selectedIds = mutableSetOf<Int>()

    fun enterSelectMode(firstId: Int) {
        isSelectMode = true
        selectedIds.clear()
        selectedIds.add(firstId)
        onSelectionChanged(selectedIds.size)
        notifyDataSetChanged()
    }

    fun exitSelectMode() {
        isSelectMode = false
        selectedIds.clear()
        onSelectionChanged(0)
        notifyDataSetChanged()
    }

    fun toggleSelection(id: Int) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
        onSelectionChanged(selectedIds.size)
        notifyDataSetChanged()
    }

    fun selectAll(notes: List<Note>) {
        if (selectedIds.size == notes.size) selectedIds.clear()
        else selectedIds.addAll(notes.map { it.id })
        onSelectionChanged(selectedIds.size)
        notifyDataSetChanged()
    }

    fun getSelectedIds(): Set<Int> = selectedIds.toSet()

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val handler = Handler(Looper.getMainLooper())
        private var holdRunnable: Runnable? = null

        fun bind(note: Note) {
            binding.tvTitle.text = note.title
            binding.tvContent.text = note.content.take(100)
            binding.tvTimestamp.text = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
                .format(Date(note.timestamp))

            val isSelected = selectedIds.contains(note.id)

            // Show checkbox only in select mode; hide action buttons in select mode
            binding.checkbox.visibility = if (isSelectMode) View.VISIBLE else View.GONE
            binding.btnDelete.visibility = if (isSelectMode) View.GONE else View.VISIBLE
            binding.btnRename.visibility = if (isSelectMode) View.GONE else View.VISIBLE
            binding.checkbox.isChecked = isSelected
            binding.root.isActivated = isSelected

            // Long-press via touch to enter select mode
            binding.root.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (!isSelectMode) {
                            holdRunnable = Runnable { onLongPress(note) }
                            handler.postDelayed(holdRunnable!!, 450)
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        holdRunnable?.let { handler.removeCallbacks(it) }
                        holdRunnable = null
                    }
                }
                false
            }

            binding.root.setOnClickListener {
                if (isSelectMode) {
                    toggleSelection(note.id)
                } else {
                    onNoteClick(note)
                }
            }

            binding.btnDelete.setOnClickListener { onNoteDelete(note) }
            binding.btnRename.setOnClickListener { onNoteRename(note) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }
}

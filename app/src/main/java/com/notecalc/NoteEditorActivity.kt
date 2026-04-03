package com.notecalc

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.notecalc.databinding.ActivityNoteEditorBinding
import net.objecthunter.exp4j.ExpressionBuilder

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditorBinding
    private lateinit var viewModel: NoteViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var currentNoteId: Int = -1
    private var calculatorExpression = StringBuilder()
    private var isScientificMode = false
    private var lastWasResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        currentNoteId = intent.getIntExtra("NOTE_ID", -1)
        if (currentNoteId != -1) {
            viewModel.getNoteById(currentNoteId).observe(this) { note ->
                note?.let {
                    binding.editNoteTitle.text = it.title
                    binding.editNoteContent.setText(it.content)
                }
            }
        }

        setupBottomSheet()
        setupCalculatorButtons()

        binding.btnBack.setOnClickListener { saveAndExit() }
        binding.btnSave.setOnClickListener { saveAndExit() }
        binding.btnRename.setOnClickListener { showRenameDialog() }
    }

    private fun showRenameDialog() {
        val input = EditText(this).apply {
            setText(binding.editNoteTitle.text)
            setSingleLine()
            setPadding(48, 32, 48, 16)
        }
        AlertDialog.Builder(this)
            .setTitle("Rename note")
            .setView(input)
            .setPositiveButton("Rename") { _, _ ->
                val t = input.text.toString().trim()
                if (t.isNotEmpty()) {
                    binding.editNoteTitle.text = t
                    Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
        input.requestFocus()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.calculatorBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 64

        binding.calculatorHandle.setOnClickListener { toggleCalculator() }
        binding.btnToggleCalculator.setOnClickListener { toggleCalculator() }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(v: android.view.View, s: Int) {
                binding.btnToggleCalculator.setImageResource(
                    if (s == BottomSheetBehavior.STATE_EXPANDED) R.drawable.ic_keyboard_arrow_down
                    else R.drawable.ic_calculate
                )
            }
            override fun onSlide(v: android.view.View, offset: Float) {}
        })
    }

    private fun toggleCalculator() {
        bottomSheetBehavior.state =
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_COLLAPSED
            else BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupCalculatorButtons() {
        fun upd() { binding.calcDisplay.text = if (calculatorExpression.isEmpty()) "0" else calculatorExpression.toString() }

        listOf(binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
               binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
               binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
               binding.btn9 to "9", binding.btnDot to ".").forEach { (btn, v) ->
            btn.setOnClickListener { if (lastWasResult) { calculatorExpression.clear(); lastWasResult = false }; calculatorExpression.append(v); upd() }
        }
        listOf(binding.btnPlus to "+", binding.btnMinus to "-",
               binding.btnMultiply to "*", binding.btnDivide to "/").forEach { (btn, op) ->
            btn.setOnClickListener { lastWasResult = false; calculatorExpression.append(op); upd() }
        }
        binding.btnSin.setOnClickListener { calculatorExpression.append("sin("); upd() }
        binding.btnCos.setOnClickListener { calculatorExpression.append("cos("); upd() }
        binding.btnTan.setOnClickListener { calculatorExpression.append("tan("); upd() }
        binding.btnLog.setOnClickListener { calculatorExpression.append("log("); upd() }
        binding.btnLn.setOnClickListener { calculatorExpression.append("ln("); upd() }
        binding.btnSqrt.setOnClickListener { calculatorExpression.append("sqrt("); upd() }
        binding.btnPow.setOnClickListener { calculatorExpression.append("^"); upd() }
        binding.btnOpenParen.setOnClickListener { calculatorExpression.append("("); upd() }
        binding.btnCloseParen.setOnClickListener { calculatorExpression.append(")"); upd() }
        binding.btnPi.setOnClickListener { calculatorExpression.append("π"); upd() }
        binding.btnClear.setOnClickListener { calculatorExpression.clear(); lastWasResult = false; upd() }
        binding.btnBackspace.setOnClickListener { if (calculatorExpression.isNotEmpty()) { calculatorExpression.deleteCharAt(calculatorExpression.length - 1); upd() } }

        binding.btnEquals.setOnClickListener {
            try {
                val expr = calculatorExpression.toString().replace("π", Math.PI.toString())
                val result = ExpressionBuilder(expr).build().evaluate()
                val s = if (result == result.toLong().toDouble()) result.toLong().toString()
                        else "%.8f".format(result).trimEnd('0').trimEnd('.')
                binding.calcDisplay.text = s
                binding.calcResult.text = calculatorExpression.toString()
                calculatorExpression.clear(); calculatorExpression.append(s); lastWasResult = true
            } catch (e: Exception) { binding.calcDisplay.text = "Error"; calculatorExpression.clear() }
        }

        binding.btnInsertResult.setOnClickListener {
            val r = binding.calcDisplay.text.toString()
            if (r != "0" && r != "Error") {
                val pos = binding.editNoteContent.selectionEnd
                val t = binding.editNoteContent.text.toString()
                binding.editNoteContent.setText(t.substring(0, pos) + r + t.substring(pos))
                binding.editNoteContent.setSelection(pos + r.length)
                Toast.makeText(this, "Result inserted", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnToggleScientific.setOnClickListener {
            isScientificMode = !isScientificMode
            binding.scientificPanel.visibility = if (isScientificMode) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnToggleScientific.text = if (isScientificMode) "Basic" else "Scientific"
        }
    }

    private fun saveAndExit() {
        val title = binding.editNoteTitle.text.toString().trim()
        val content = binding.editNoteContent.text.toString().trim()
        if (title.isEmpty() && content.isEmpty()) { finish(); return }
        val note = Note(
            id = if (currentNoteId == -1) 0 else currentNoteId,
            title = title.ifEmpty { "Untitled" },
            content = content,
            timestamp = System.currentTimeMillis()
        )
        if (currentNoteId == -1) viewModel.insert(note) else viewModel.update(note)
        finish()
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        else saveAndExit()
    }
}

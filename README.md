# NoteCalc 📝🧮

A notes app with a **sliding bottom-sheet calculator** built in Kotlin for Android.

---

## Features
- ✅ Create, edit, delete notes (saved with Room DB)
- ✅ Slide-up calculator bottom sheet (drag handle or tap icon)
- ✅ Basic calculator: + − × ÷
- ✅ Scientific mode: sin, cos, tan, log, ln, √, ^, π, parentheses
- ✅ **Insert result** button pastes answer into your note at cursor position

---

## Setup in Android Studio

### 1. Open Project
- File → Open → select the `NoteCalc` folder

### 2. Add Font Files
In `app/src/main/res/font/`, add:
- `inter.ttf`
- `inter_bold.ttf`

Download from: https://fonts.google.com/specimen/Inter

> Or remove `android:fontFamily` attributes from layouts/styles to use system fonts.

### 3. Add Vector Icons
In `app/src/main/res/drawable/`, add these vector drawables using
**Android Studio → Resource Manager → + → Vector Asset**:
- `ic_add.xml` (Material: Add)
- `ic_arrow_back.xml` (Material: Arrow Back)
- `ic_delete.xml` (Material: Delete)
- `ic_calculate.xml` (Material: Calculate)
- `ic_keyboard_arrow_down.xml` (Material: Keyboard Arrow Down)

### 4. Sync & Run
- Click **Sync Now** when prompted
- Run on emulator or device (API 26+)

---

## Architecture
```
MainActivity          → Lists all notes
NoteEditorActivity    → Edit note + sliding calculator
Note.kt               → Room @Entity
NoteDao.kt            → DB queries
NoteDatabase.kt       → Room DB singleton
NoteRepository.kt     → Data layer
NoteViewModel.kt      → UI state
NoteAdapter.kt        → RecyclerView adapter
```

---

## Calculator Logic
Uses the **exp4j** library for expression evaluation.
- Supports: `sin(`, `cos(`, `tan(`, `log(`, `ln(`, `sqrt(`, `^`, `π`
- Result precision: trimmed trailing zeros
- Error handling: shows "Error" on invalid expression

---

## Dependencies (app/build.gradle)
| Library | Purpose |
|---|---|
| Room | Local notes database |
| ViewModel + LiveData | Lifecycle-aware UI |
| Material Components | UI widgets & bottom sheet |
| exp4j 0.4.8 | Math expression parser |
| ViewBinding | Safe view access |

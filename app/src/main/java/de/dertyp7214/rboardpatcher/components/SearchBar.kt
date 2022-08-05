@file:Suppress("unused")

package de.dertyp7214.rboardpatcher.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doOnTextChanged
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import de.dertyp7214.rboardpatcher.R

@Suppress("MemberVisibilityCanBePrivate")
@SuppressLint("ResourceType")
class SearchBar(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    var focus = false
        private set
    private var searchListener: (text: String) -> Unit = {}
    private var closeListener: () -> Unit = {}
    private var focusListener: () -> Unit = {}
    private var suggestionClickListener: (suggestion: String) -> Unit = {}
    private var menuListener: (ImageButton) -> Unit = {}

    private var popupMenu: PopupMenu? = null
    private var menuItemClickListener: PopupMenu.OnMenuItemClickListener? = null

    private val searchBar: MaterialCardView by lazy { findViewById(R.id.search_bar) }
    private val searchButton: ImageButton by lazy { findViewById(R.id.search_button) }
    private val backButton: ImageButton by lazy { findViewById(R.id.back_button) }
    private val moreButton: ImageButton by lazy { findViewById(R.id.more_button) }
    private val searchText: TextView by lazy { findViewById(R.id.search_text) }
    private val searchEdit: MaterialAutoCompleteTextView by lazy { findViewById(R.id.search) }

    var instantSearch: Boolean = false

    var text: String = ""
        set(value) {
            field = value
            if (value.isEmpty()) {
                focus = false
                searchButton.visibility = VISIBLE
                backButton.visibility = GONE

                searchText.visibility = VISIBLE
                searchEdit.visibility = GONE
            } else {
                focus = true
                searchButton.visibility = GONE
                backButton.visibility = VISIBLE

                searchText.visibility = GONE
                searchEdit.visibility = VISIBLE
            }

            searchEdit.setText(value)
            clearFocus()
        }

    var suggestions: List<String> = listOf()
        set(value) {
            field = value

            searchEdit.setSimpleItems(value.toTypedArray())
        }

    var menuVisible: Boolean = true
        set(value) {
            field = value
            if (value) searchButton.setImageResource(R.drawable.ic_hamburger)
            else searchButton.setImageResource(R.drawable.ic_baseline_search_24)
        }

    init {
        inflate(context, R.layout.search_bar, this)

        moreButton.visibility = INVISIBLE

        searchBar.setOnClickListener {
            if (!focus) {
                focus = true
                searchButton.visibility = GONE
                backButton.visibility = VISIBLE

                searchText.visibility = GONE
                searchEdit.visibility = VISIBLE

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    searchEdit.windowInsetsController?.show(WindowInsets.Type.ime())
                searchEdit.requestFocus()
                focusListener()
            }
        }

        backButton.setOnClickListener {
            if (focus) {
                text = ""
                closeListener()
            }
        }

        moreButton.setOnClickListener {
            popupMenu?.show()
        }

        searchEdit.doOnTextChanged { text, _, _, _ ->
            if (instantSearch) searchListener(text?.toString() ?: "")
        }

        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                clearFocus()
                text = searchEdit.text.toString()
                searchListener(text)
                true
            } else false
        }

        searchButton.setOnClickListener {
            if (menuVisible) menuListener(searchButton)
            else searchBar.callOnClick()
        }

        searchEdit.setSimpleItems(suggestions.toTypedArray())

        searchEdit.setOnItemClickListener { _, _, position, _ ->
            suggestionClickListener(suggestions[position])
        }

        searchEdit.setOnFocusChangeListener { _, focused ->
            if (focused) focusListener()
        }
    }

    fun setMenu(
        @MenuRes menu: Int? = null,
        itemClickListener: PopupMenu.OnMenuItemClickListener? = null
    ) {
        popupMenu = if (menu != null) {
            moreButton.visibility = VISIBLE
            PopupMenu(context, moreButton).also { popup ->
                popup.menuInflater.inflate(menu, popup.menu)
                popup.setOnMenuItemClickListener(itemClickListener)
            }
        } else {
            moreButton.visibility = INVISIBLE
            null
        }
    }

    fun focus() {
        searchBar.performClick()
    }

    fun showSuggestions() {
        searchEdit.showDropDown()
    }

    fun search() {
        clearFocus()
        searchListener(searchEdit.text.toString())
    }

    fun clearText() = ::text.set("")

    fun close() {
        clearText()
        closeListener()
    }

    fun setOnSearchListener(listener: (text: String) -> Unit) {
        searchListener = listener
    }

    fun setOnCloseListener(listener: () -> Unit) {
        closeListener = listener
    }

    fun setOnFocusListener(listener: () -> Unit) {
        focusListener = listener
    }

    fun setOnSuggestionClickListener(listener: (suggestion: String) -> Unit) {
        suggestionClickListener = listener
    }

    fun setOnMenuListener(listener: (ImageButton) -> Unit) {
        menuListener = listener
    }

    override fun clearFocus() {
        super.clearFocus()
        clearFocus(searchEdit)
    }

    private fun clearFocus(editText: EditText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Handler(Looper.getMainLooper()).postDelayed({
                editText.windowInsetsController?.hide(WindowInsets.Type.ime())
            }, 100)
        }
    }
}
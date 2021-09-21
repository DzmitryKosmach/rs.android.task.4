package rs.android.task4.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import rs.android.task4.*
import rs.android.task4.adapter.AnimalAdapter
import rs.android.task4.adapter.OnAnimalItemClickListener
import rs.android.task4.databinding.AnimalsFragmentBinding
//import rs.android.task4.db.dao.Animal
//import rs.android.task4.db.dao.AnimalDao
import rs.android.task4.repository.Animal

class AnimalsFragment : Fragment(R.layout.animals_fragment), OnAnimalItemClickListener {

    private var binding: AnimalsFragmentBinding? = null
    private val viewModel: AnimalsViewModel by viewModels()
    private val adapter: AnimalAdapter? get() = views { recyclerView.adapter as? AnimalAdapter }
    private var menuItemDatabase: MenuItem? = null
    private var prefs: SharedPreferences? = null

    interface Callbacks {
        fun onFragmentMenuSortClick()
        fun onFragmentAddClick()
        fun onFragmentAddToChangeClick(item: Animal)
    }

    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = AnimalsFragmentBinding.inflate(inflater).also { binding = it }.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.activity?.title = resources.getString(R.string.bar_title_fragment_animals)
        val animalAdapter = AnimalAdapter(this)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        viewModel.animalsFlow.onEach(::renderAnimals).launchIn(lifecycleScope)

        viewModel.sourceDbFlow.onEach(::changeDbSource).launchIn(lifecycleScope)

        val sourceDB = prefs?.getString(
            resources.getString(R.string.key_source_db),
            resources.getString(R.string.key_source_room_db)
        )

        if (sourceDB != null) {
            viewModel.setDbSource(sourceDB)
        }

        val sortingColumn = (prefs?.getString(
            resources.getString(R.string.key_column), resources.getString(
                R.string.key_column_name_name
            )
        ).toString())

        viewModel.setSortBy(sortingColumn)

        views {
            recyclerView.adapter = animalAdapter
            floatingButton.setOnClickListener { _ ->
                callbacks?.onFragmentAddClick()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        renderAnimals(viewModel.animalsFlow.replayCache.flatten())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.animals_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
        menuItemDatabase = menu.getItem(0)

        val sourceDB = prefs?.getString(
            resources.getString(R.string.key_source_db),
            DEFAULT_PREF_DATABASE_NAME
        )
        if (sourceDB == resources.getString(R.string.key_source_room_db)){
            menuItemDatabase?.icon = resources.getDrawable(R.drawable.ic_bar_database_r)
        }else {
            menuItemDatabase?.icon = resources.getDrawable(R.drawable.ic_bar_database_c)
        }
        viewModel.setDbSource(sourceDB!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.animals_fragment_menu_filter -> {
                callbacks?.onFragmentMenuSortClick()
                return true
            }
            R.id.animals_fragment_menu_database -> {
                var sourceDB = prefs?.getString(
                    resources.getString(R.string.key_source_db),
                    DEFAULT_PREF_DATABASE_NAME
                )
                if (sourceDB == resources.getString(R.string.key_source_room_db)){
                    menuItemDatabase?.icon = resources.getDrawable(R.drawable.ic_bar_database_c)
                    sourceDB = DATABASE_SOURCE_NAME_CURSOR
                }else {
                    menuItemDatabase?.icon = resources.getDrawable(R.drawable.ic_bar_database_r)
                    sourceDB = DATABASE_SOURCE_NAME_ROOM
                }
                prefs?.edit()?.putString(
                    resources.getString(R.string.key_source_db),
                    sourceDB
                )?.apply()
                viewModel.setDbSource(sourceDB)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onItemClick(item: Animal) {
        callbacks?.onFragmentAddToChangeClick(item)
    }

    private fun renderAnimals(animals: List<Animal>) {
        adapter?.submitList(animals)
    }

    private fun changeDbSource(dbSource: String) {
        viewModel.changeDatabaseSource(dbSource)
    }

    private fun <T> views(block: AnimalsFragmentBinding.() -> T): T? = binding?.block()

}

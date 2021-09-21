package rs.android.task4.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import rs.android.task4.MainActivity
import rs.android.task4.R
import rs.android.task4.databinding.AddFragmentBinding
import rs.android.task4.databinding.SortFragmentBinding

class SortFragment : Fragment() {

    private var binding: SortFragmentBinding? = null

    private var prefs: SharedPreferences? = null

    interface Callbacks {
        fun onFragmentAnimalsClick()
    }

    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SortFragmentBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = resources.getString(R.string.bar_title_fragment_sort)

        prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val sortingBy = prefs?.getString(
            resources.getString(R.string.key_column),
            resources.getString(R.string.key_column_name_name)
        )
        val id = resources.getIdentifier(
            sortingBy,
            resources.getString(R.string.view_id),
            context?.packageName
        )
        if (id != 0) {
            val button = view.findViewById<Button>(id)
            button.setTextAppearance(context, R.style.AddFragmentItemsSortedBy)
        }

        views {

            name.setOnClickListener { _ ->
                setSorting(resources.getString(R.string.key_column_name_name))
            }

            age.setOnClickListener { _ ->
                setSorting(resources.getString(R.string.key_column_name_age))
            }

            breed.setOnClickListener { _ ->
                setSorting(resources.getString(R.string.key_column_name_breed))
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setSorting(sorting: String) {
        prefs?.edit()?.putString(
            resources.getString(R.string.key_column),
            sorting
        )?.apply()
        callbacks?.onFragmentAnimalsClick()
    }

    private fun <T> views(block: SortFragmentBinding.() -> T): T? = binding?.block()

}
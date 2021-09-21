package rs.android.task4.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import rs.android.task4.KEY_ANIMAL

import rs.android.task4.MainActivity
import rs.android.task4.R
import rs.android.task4.databinding.AddFragmentBinding
import rs.android.task4.repository.Animal

class AddFragment : Fragment(R.layout.add_fragment) {
    private var binding: AddFragmentBinding? = null
    private val viewModel: AddViewModel by viewModels()

    interface Callbacks {
        fun onBackFragmentAnimals()
    }

    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = AddFragmentBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var animal: Animal? = null

        arguments?.let {
            animal = it.getParcelable(KEY_ANIMAL)
        }

        views {
            if (animal == null) {
                activity?.title = resources.getString(R.string.bar_title_fragment_add)

                addButton.setOnClickListener { _ ->

                    val name = textInputName.text.toString()
                    val age = textInputAge.text.toString()
                    val breed = textInputBreed.text.toString()
                    performOrAlertDialogFillAllFields(name, age, breed) {
                        viewModel.save(name, age.toInt(), breed)
                        callbacks?.onBackFragmentAnimals()
                    }
                }
            } else {
                activity?.title = resources.getString(R.string.bar_title_fragment_edit)

                addButton.visibility = View.INVISIBLE
                updateButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE

                val currentAnimal = animal as Animal
                textInputName.setText(currentAnimal.name)
                textInputAge.setText(currentAnimal.age.toString())
                textInputBreed.setText(currentAnimal.breed)

                updateButton.setOnClickListener { _ ->
                    val name = textInputName.text.toString()
                    val age = textInputAge.text.toString()
                    val breed = textInputBreed.text.toString()
                    performOrAlertDialogFillAllFields(name, age, breed) {
                        val updatedAnimal = Animal(currentAnimal.id, name, age.toInt(), breed)
                        viewModel.update(updatedAnimal)
                        callbacks?.onBackFragmentAnimals()
                    }
                }

                deleteButton.setOnClickListener { _ ->

                    AlertDialog.Builder(activity).apply {
                        setTitle(R.string.app_name)
                        setMessage(resources.getString(R.string.info_fragment_delete))
                        setPositiveButton(resources.getString(R.string.alert_dialog_button_yes)) { _, _ ->
                            viewModel.delete(animal!!)
                            callbacks?.onBackFragmentAnimals()
                        }
                        setNegativeButton(resources.getString(R.string.alert_dialog_button_no)) { _, _ ->
                        }
                    }.create().show()
                }
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

    private fun performOrAlertDialogFillAllFields(
        name: String,
        age: String,
        breed: String,
        action: () -> Unit
    ) {
        views {
            if (name.isNotBlank() && age.isNotBlank() && breed.isNotBlank()) {
                action()
            } else {
                AlertDialog.Builder(activity).apply {
                    setTitle(R.string.app_name)
                    setMessage(resources.getString(R.string.info_fragment_add))
                    setPositiveButton(resources.getString(R.string.alert_dialog_button_ok)) { _, _ -> }
                }.create().show()
            }
        }
    }

    private fun <T> views(block: AddFragmentBinding.() -> T): T? = binding?.block()

}
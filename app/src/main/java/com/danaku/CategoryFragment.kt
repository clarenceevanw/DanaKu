package com.danaku

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.danaku.databinding.DialogCategoryFormBinding
import com.danaku.databinding.DialogCustomAlertBinding
import com.danaku.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var storage: FinanceStorage
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = FinanceStorage(requireContext())

        setupRecyclerView()
        setupListeners()
        loadCategories()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            categories = emptyList(),
            onEditClick = { category -> showFormDialog(category) },
            onDeleteClick = { category -> confirmDeleteCategory(category) }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAddCategory.setOnClickListener {
            showFormDialog(null)
        }
    }

    private fun loadCategories() {
        val categories = storage.getCategories()
        adapter.updateData(categories)
        binding.tvEmptyState.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showFormDialog(categoryToEdit: Category?) {
        val dialogBinding = DialogCategoryFormBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val isEdit = categoryToEdit != null
        dialogBinding.tvDialogTitle.text = getString(if (isEdit) R.string.action_edit else R.string.action_add)

        if (isEdit) {
            dialogBinding.etCategoryName.setText(categoryToEdit.name)
            if (categoryToEdit.type == TransactionType.INCOME) {
                dialogBinding.rbIncome.isChecked = true
            } else {
                dialogBinding.rbExpense.isChecked = true
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etCategoryName.text.toString().trim()
            if (name.isEmpty()) {
                dialogBinding.tvErrorName.text = getString(R.string.field_required)
                dialogBinding.tvErrorName.visibility = View.VISIBLE
                return@setOnClickListener
            }
            dialogBinding.tvErrorName.visibility = View.GONE

            val selectedType = if (dialogBinding.rbIncome.isChecked) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }

            val success = if (isEdit) {
                storage.updateCategory(categoryToEdit.copy(name = name, type = selectedType))
            } else {
                storage.addCategory(name = name, type = selectedType)
            }

            if (success) {
                loadCategories()
                dialog.dismiss()
            } else {
                dialogBinding.tvErrorName.text = getString(R.string.category_already_exists)
                dialogBinding.tvErrorName.visibility = View.VISIBLE
            }
        }

        dialog.show()
    }

    private fun confirmDeleteCategory(category: Category) {
        val alertBinding = DialogCustomAlertBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(alertBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        if (storage.isCategoryUsed(category.id)) {
            alertBinding.tvAlertTitle.text = getString(R.string.delete_category_blocked_title)
            alertBinding.tvAlertMessage.text = getString(R.string.delete_category_blocked_message)
            alertBinding.btnAlertPositive.text = "OK"
            alertBinding.btnAlertPositive.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.neon_green)
            alertBinding.btnAlertPositive.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            alertBinding.btnAlertNegative.visibility = View.GONE

            alertBinding.btnAlertPositive.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
            return
        }

        alertBinding.tvAlertTitle.text = getString(R.string.delete_confirm_title)
        alertBinding.tvAlertMessage.text = getString(R.string.delete_category_message)
        alertBinding.btnAlertNegative.visibility = View.VISIBLE
        alertBinding.btnAlertNegative.setOnClickListener {
            dialog.dismiss()
        }

        alertBinding.btnAlertPositive.text = getString(R.string.action_delete)
        alertBinding.btnAlertPositive.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.expense_red)
        alertBinding.btnAlertPositive.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        alertBinding.btnAlertPositive.setOnClickListener {
            val deleted = storage.deleteCategory(category.id)
            if (deleted) {
                loadCategories()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Gagal menghapus kategori", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

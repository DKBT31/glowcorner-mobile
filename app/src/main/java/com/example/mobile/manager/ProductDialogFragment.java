package com.example.mobile.manager;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Adapter.ProductAdapter;
import com.example.mobile.Models.Product;
import com.example.mobile.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductDialogFragment extends DialogFragment {
    private List<Product> allProducts;
    private List<Product> selectedProducts;
    private OnProductSelectionListener listener;
    private ProductAdapter adapter;
    private EditText etSearch;

    public interface OnProductSelectionListener {
        void onProductSelectionChanged(Set<String> selectedProductIDs);
    }

    public static ProductDialogFragment newInstance(List<Product> products, List<Product> selectedProducts) {
        ProductDialogFragment fragment = new ProductDialogFragment();
        Bundle args = new Bundle();
        // Cast to ArrayList<? extends Parcelable> by ensuring the list contains Parcelable objects
        args.putParcelableArrayList("products", new ArrayList<Product>(products));
        args.putParcelableArrayList("selectedProducts", new ArrayList<Product>(selectedProducts));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Safe cast from ArrayList<? extends Parcelable> to ArrayList<Product>
            allProducts = new ArrayList<>(getArguments().getParcelableArrayList("products"));
            selectedProducts = new ArrayList<>(getArguments().getParcelableArrayList("selectedProducts"));
            if (allProducts == null) allProducts = new ArrayList<>();
            if (selectedProducts == null) selectedProducts = new ArrayList<>();
        } else {
            allProducts = new ArrayList<>();
            selectedProducts = new ArrayList<>();
        }
        Set<String> initialSelected = new HashSet<>();
        if (selectedProducts != null) {
            initialSelected.addAll(selectedProducts.stream().map(Product::getProductID).collect(Collectors.toSet()));
        }
        adapter = new ProductAdapter(new ArrayList<>(allProducts));
        adapter.setInitialSelectedProductIDs(initialSelected);
        adapter.setOnSelectionChangedListener(selectedIds -> {
            selectedProducts.clear();
            selectedProducts.addAll(allProducts.stream()
                    .filter(p -> selectedIds.contains(p.getProductID()))
                    .collect(Collectors.toList()));
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_product_selection, null);

        etSearch = view.findViewById(R.id.et_search);
        RecyclerView rvProducts = view.findViewById(R.id.rv_products);
        Button btnApply = view.findViewById(R.id.btn_apply);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProducts.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                List<Product> filtered = allProducts.stream()
                        .filter(p -> p.getProductName().toLowerCase().contains(query) ||
                                p.getProductID().toLowerCase().contains(query) ||
                                (p.getCategory() != null && p.getCategory().toLowerCase().contains(query)))
                        .collect(Collectors.toList());
                adapter.getFilter().filter(query); // Use adapter's filter
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductSelectionChanged(adapter.getSelectedProductIDs());
            }
            dismiss();
        });
        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    public void setProductSelectionListener(OnProductSelectionListener listener) {
        this.listener = listener;
    }
}
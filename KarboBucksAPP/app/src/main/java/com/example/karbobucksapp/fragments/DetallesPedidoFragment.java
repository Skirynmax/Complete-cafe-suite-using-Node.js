package com.example.karbobucksapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karbobucksapp.R;
import com.example.karbobucksapp.models.Pedido;

public class DetallesPedidoFragment extends Fragment {

    private TextView txtEstatus, txtTiempo, txtTotal;
    private RecyclerView rvProducts;
    private Pedido pedido;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalles_pedido, container, false);

        iniciarViews(view);

        if (getArguments() != null) {
            pedido = (Pedido) getArguments().getSerializable("order");
            if (pedido != null) {
                mostrarDetallesPedido();
            }
        }

        return view;
    }

    private void iniciarViews(View view) {
        txtEstatus = view.findViewById(R.id.tv_detail_status);
        txtTiempo = view.findViewById(R.id.tv_detail_time);
        txtTotal = view.findViewById(R.id.tv_detail_total);
        rvProducts = view.findViewById(R.id.rv_products);

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void mostrarDetallesPedido() {
        txtEstatus.setText(pedido.getEstado());
        txtTiempo.setText(pedido.getTiempoEstimado()/60 + " min");
        txtTotal.setText(String.format("%.2f€", pedido.getTotalPagar()));

        // Configurar adapter para RecyclerView
        ProductAdapter adapter = new ProductAdapter(pedido.getProductos());
        rvProducts.setAdapter(adapter);
    }

    // Adapter para la lista de productos
    private static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

        private final java.util.List<Pedido.ProductItem> products;

        ProductAdapter(java.util.List<Pedido.ProductItem> products) {
            this.products = products;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.pedido_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Pedido.ProductItem product = products.get(position);
            holder.tvProductName.setText(product.getNombre());
            holder.tvProductPrice.setText(String.format("%.2f€", product.getPrecio()));
        }

        @Override
        public int getItemCount() {
            return products != null ? products.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProductName, tvProductPrice;

            ViewHolder(View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tv_product_name);
                tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            }
        }
    }
}
package group.sw.spos.lib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import group.sw.spos.R;

public class BillUploadProductListAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<BillProductList> mBillProducts;
    private ArrayList<BillProductList> filterBillProducts;
    BillUploadProductListAdapter.CustomBillProduct filter;

    public BillUploadProductListAdapter(Context mContext, ArrayList<BillProductList> mBillProducts) {
        this.mContext = mContext;
        this.mBillProducts = mBillProducts;
        this.filterBillProducts = mBillProducts;
    }

    @Override
    public int getCount() {
        return mBillProducts.size();
    }

    @Override
    public Object getItem(int position) {
        return mBillProducts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter(){
        if(filter == null){
            filter = new BillUploadProductListAdapter.CustomBillProduct();
        }
        return filter;
    }

    class CustomBillProduct extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() >0) {
                constraint = constraint.toString().toUpperCase();
                ArrayList<BillProductList> filters = new ArrayList<BillProductList>();
                for(int i=0; i <filterBillProducts.size(); i++) {
                    if (filterBillProducts.get(i).getName().toUpperCase().contains(constraint) || filterBillProducts.get(i).getCode().toUpperCase().contains(constraint)) {
                        BillProductList billProductList = new BillProductList(
                                filterBillProducts.get(i).getId(),
                                filterBillProducts.get(i).getBillNo(),
                                filterBillProducts.get(i).getName(),
                                filterBillProducts.get(i).getCode(),
                                filterBillProducts.get(i).getUnit(),
                                filterBillProducts.get(i).getQuantity(),
                                filterBillProducts.get(i).isIsclicked(),
                                filterBillProducts.get(i).getCreateDate()
                        );
                        filters.add(billProductList);
                    }
                }
                results.count = filters.size();
                results.values = filters;
            } else {
                results.count = filterBillProducts.size();
                results.values = filterBillProducts;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results){
            mBillProducts = (ArrayList<BillProductList>) results.values;
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.upload_product_item, parent, false);
        }
        TextView goodNameView = (TextView) convertView.findViewById(R.id.good_name);
        TextView unitView = (TextView) convertView.findViewById(R.id.unit);
        TextView quantityView = (TextView) convertView.findViewById(R.id.quantity);
        BillProductList billProductList = mBillProducts.get(position);
        goodNameView.setText(billProductList.getName());
        unitView.setText(billProductList.getUnit());
        quantityView.setText(String.valueOf(billProductList.getQuantity()));

        return convertView;
    }
}

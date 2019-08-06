package group.sw.spos.lib;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.util.ArrayList;
import group.sw.spos.R;


public class BillListAdapter extends BaseAdapter implements Filterable {
    private Context mContext;
    private ArrayList<BillList> mBills;
    private ArrayList<BillList> filterBills;

    CustomBillFilter filter;

    public BillListAdapter(Context mContext, ArrayList<BillList> mBills) {
        this.mContext = mContext;
        this.mBills = mBills;
        this.filterBills = mBills;
    }

    @Override
    public int getCount() {
        return mBills.size();
    }

    @Override
    public Object getItem(int position) {
        return mBills.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter(){
        if(filter == null){
            filter = new CustomBillFilter();
        }
        return filter;
    }

    class CustomBillFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint){
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() >0) {
                constraint = constraint.toString();
                Log.d("string", String.valueOf(constraint));
                ArrayList<BillList> filters = new ArrayList<BillList>();
                for(int i=0; i <filterBills.size(); i++) {
                    if (filterBills.get(i).getNo().toUpperCase().contains(constraint)) {
                        BillList userBalanceList = new BillList(
                                filterBills.get(i).getNo(),
                                filterBills.get(i).getDatTime(),
                                filterBills.get(i).getStatus(),
                                filterBills.get(i).getTimestamp(),
                                filterBills.get(i).getWarehouse(),
                                filterBills.get(i).getWarehoseName());
                        filters.add(userBalanceList);
                    }
                }
                results.count = filters.size();
                results.values = filters;
            } else {
                results.count = filterBills.size();
                results.values = filterBills;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results){
            mBills = (ArrayList<BillList>) results.values;
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bill_item, parent, false);
        }
        TextView tvBillNo = (TextView) convertView.findViewById(R.id.bill_no);
        TextView tvDate = (TextView) convertView.findViewById(R.id.bill_date);
        TextView tvStatus = (TextView) convertView.findViewById(R.id.bill_status);
        tvBillNo.setText(mBills.get(position).getNo());
        tvDate.setText(mBills.get(position).getDatTime());
        tvStatus.setText(mBills.get(position).getStatus());
        convertView.setTag(mBills.get(position).getNo());
        return convertView;
    }


    public void updateRecords(ArrayList<BillList> billLists){
        this.mBills = billLists;
        this.filterBills = billLists;
        notifyDataSetChanged();
    }
}

/**************************************************************************************************
 * Copyright (c) 2016-present, Produvia, LTD.
 * All rights reserved.
 * This source code is licensed under the MIT license
 **************************************************************************************************/

package produvia.com.scanner;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;



public class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {

    public static final int VIEW_TYPE_TITLE = 0;
    public static final int VIEW_TYPE_NORMAL = 1;
    private static final int[] mColorsArray= new int[] {0xff99cc00, 0xffff8800, 0xff0099cc, 0xffcc0000};

    public interface CustomListCallbacks {
        void onItemClicked(CustomListItem item, View v, int position);
        void onToggleClicked(CustomListItem item, boolean checked);
        void onLeftImageClicked(CustomListItem item, View v, int position);
    }

    public CustomListCallbacks mCallbacks;



    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtTitle;
        public ImageView leftImageView;
        public TextView descriptionTextView;
        public TextView  statusTextView;
        public View detailsView;
        public android.widget.ToggleButton toggleButton;
        public TableLayout detailsTable;
        public ImageView transportImageView;


        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle = (TextView) itemView.findViewById(R.id.title);
            leftImageView = (ImageView) itemView.findViewById(R.id.thumbnail);

            descriptionTextView = (TextView) itemView.findViewById(R.id.description);
            toggleButton = (android.widget.ToggleButton)itemView.findViewById(R.id.toggleButton);
            detailsView = itemView.findViewById(R.id.details);
            statusTextView = (TextView)itemView.findViewById(R.id.status);
            detailsTable = (TableLayout) itemView.findViewById(R.id.details_table);
            transportImageView = (ImageView)itemView.findViewById(R.id.transport);

        }
    }



    @Override
    public int getItemViewType(int position) {
        //Implement your logic here
        CustomListItem item = mArray.get(position);

        return item.isTitle()?VIEW_TYPE_TITLE:VIEW_TYPE_NORMAL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(viewType == VIEW_TYPE_NORMAL) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_item_card, parent, false);
        } else {

            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_item_title_card, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        CustomListItem item = mArray.get(position);
        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = position;
                CustomListItem item = CustomRecyclerAdapter.this.mArray.get(itemPosition);
                if (mCallbacks != null)
                    mCallbacks.onItemClicked(item, v, itemPosition);

            }
        });

        holder.leftImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = position;
                CustomListItem item = CustomRecyclerAdapter.this.mArray.get(itemPosition);
                if (mCallbacks != null)
                    mCallbacks.onLeftImageClicked(item, v, itemPosition);
            }
        });
        //holder.leftImageView.setClickable(left_image_clickable && mListView.getCheckedItemCount()==0);
        if(getItemViewType(position) == VIEW_TYPE_NORMAL){
            if(item.showDetails()){
                holder.detailsView.setVisibility(View.VISIBLE);
            }else {
                holder.detailsView.setVisibility(View.GONE);
            }
        }


        if(item.isToggleEnabled()) {
            holder.toggleButton.setVisibility(View.VISIBLE);
            holder.toggleButton.setChecked(item.getToggle());
            final CustomListItem fitem = item;
            holder.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    fitem.setToggle( isChecked );
                    if(mCallbacks != null)
                        mCallbacks.onToggleClicked(fitem, isChecked);

                }
            });
        }

        //holder.itemView.setBackgroundColor(item.getColor());

        String name = item.getName();
        String description = item.getDescription();
        String status = item.getStatus();

        if(getItemViewType(position) == VIEW_TYPE_TITLE)
            holder.txtTitle.setTextColor(mColorsArray[position%mColorsArray.length]);

        if(name != null)
            holder.txtTitle.setText(name);
        if(description != null && holder.descriptionTextView != null)
            holder.descriptionTextView.setText(description);
        if(status != null && holder.statusTextView != null){
            holder.statusTextView.setText(status);
        }

        if(item instanceof DeviceCard){

            holder.transportImageView.setImageResource(((DeviceCard) item).getTransportIcon());

            Context context = holder.detailsView.getContext();
            ArrayList<String>tableEntries = ((DeviceCard)item).getTableEntries();

            if(holder.detailsTable.getChildCount() > 0)
                holder.detailsTable.removeAllViews();
            /* Create rows: */
            for(int i = 0; i < tableEntries.size(); i+=2) {
                TableRow tr = new TableRow(context);
                TextView tv = new TextView(context);
                tv.setText(tableEntries.get(i));
                TextView tv1 = new TextView(context);
                tv1.setText(tableEntries.get(i+1));

                tr.addView(tv);
                tr.addView(tv1);
                /* Add row to TableLayout. */
                holder.detailsTable.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            }

        }


        Integer image = item.getLeftImage();
        if( image != null ) {
            holder.leftImageView.setImageResource(image);
            //holder.leftImageView.setBackgroundColor(item.getColor());
        }
    }

    @Override
    public int getItemCount() {
        return mArray.size();
    }


    ArrayList<CustomListItem> mArray = null;
    RecyclerView mListView = null;







    public CustomRecyclerAdapter(RecyclerView lv, Activity context,
                                 ArrayList<CustomListItem> dataArray) {
        super();
        mListView = lv;
        mArray = dataArray;
    }



}
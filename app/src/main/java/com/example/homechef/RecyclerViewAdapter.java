package com.example.homechef;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{

    private Context mContext ;
    private ArrayList<Recipe> mList ;

    public RecyclerViewAdapter(Context mContext, ArrayList<Recipe> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cardview_item_recipe,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Recipe mData = mList.get(position);
        holder.tv_name.setText(mData.getName());
        if (mData.getNumServings().isEmpty()) {
            holder.layout_servings.setVisibility(View.GONE);;
        } else{
            holder.tv_servings.setText(mData.getNumServings());
        }
        if (mData.getTotalTime().isEmpty()) {
            holder.layout_time.setVisibility(View.GONE);
        } else{
            holder.tv_time.setText(mData.getTotalTime());
        }
        if (mData.getThumbnail().isEmpty()) {
            holder.img_recipe_thumbnail.setImageResource(R.drawable.nopicture);
        } else{
            Picasso.get().load(mData.getThumbnail()).into(holder.img_recipe_thumbnail);
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext,RecipeActivity.class);
                intent.putExtra("id",mData.getId());
                intent.putExtra("name",mData.getName());
                intent.putExtra("img",mData.getThumbnail());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_name,tv_servings,tv_time;
        ImageView img_recipe_thumbnail;
        CardView cardView ;
        LinearLayout layout_time, layout_servings;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.recipe_name_id) ;
            img_recipe_thumbnail = (ImageView) itemView.findViewById(R.id.recipe_img_id);
            tv_servings = (TextView) itemView.findViewById(R.id.recipe_servings_id);
            tv_time = (TextView) itemView.findViewById(R.id.recipe_time_id);
            cardView = (CardView) itemView.findViewById(R.id.cardview_id);
            layout_time = (LinearLayout) itemView.findViewById(R.id.layout_time);
            layout_servings = (LinearLayout) itemView.findViewById(R.id.layout_servings);
         }
    }
}

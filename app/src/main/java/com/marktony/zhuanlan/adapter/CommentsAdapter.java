package com.marktony.zhuanlan.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.marktony.zhuanlan.R;
import com.marktony.zhuanlan.bean.AuthorOrLiker;
import com.marktony.zhuanlan.bean.Comment;
import com.marktony.zhuanlan.view.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizhaotailang on 2016/5/19.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private final Context context;
    private final LayoutInflater inflater;

    private List<Comment> list = new ArrayList<>();

    public CommentsAdapter(Context context,List<Comment> list){
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentViewHolder(inflater.inflate(R.layout.comment_item,parent,false));
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        Comment item = list.get(position);

        AuthorOrLiker a = item.getAuthor();

        Glide.with(context)
                .load(a.getAvatar().getTemplate().replace("{id}", a.getAvatar().getId()).replace("{size}", "m"))
                .asBitmap()
                .centerCrop()
                .into(holder.avatar);

        holder.tvAuthor.setText(a.getName());
        holder.tvContent.setText(item.getContent());
        String likes = item.getLikesCount() + "赞";
        holder.tvLikes.setText(likes);
        holder.tvTime.setText(item.getCreatedTime());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView avatar;
        private TextView tvAuthor;
        private TextView tvContent;
        private TextView tvTime;
        private TextView tvLikes;

        public CommentViewHolder(View itemView) {
            super(itemView);

            avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            tvAuthor = (TextView) itemView.findViewById(R.id.tv_author);
            tvContent = (TextView) itemView.findViewById(R.id.tv_content);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            tvLikes = (TextView) itemView.findViewById(R.id.tv_likes);

        }
    }
}

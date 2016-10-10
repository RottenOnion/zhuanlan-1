package com.marktony.zhuanlan.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.marktony.zhuanlan.R;
import com.marktony.zhuanlan.adapter.ZhuanlanAdapter;
import com.marktony.zhuanlan.app.VolleySingleton;
import com.marktony.zhuanlan.bean.Zhuanlan;
import com.marktony.zhuanlan.db.MyDataBaseHelper;
import com.marktony.zhuanlan.utils.API;
import com.marktony.zhuanlan.interfaze.OnRecyclerViewOnClickListener;

import java.util.ArrayList;

/**
 * Created by lizhaotailang on 2016/5/26.
 */
public class UserDefineIdsFragment extends Fragment{

    private MyDataBaseHelper dbHelper;
    private SQLiteDatabase db;
    private ArrayList<String> list = new ArrayList<String>();

    private TextView tvUserDefine;
    private FloatingActionButton fab;
    private SwipeRefreshLayout refreshLayout;

    private ZhuanlanAdapter adapter;
    private ArrayList<Zhuanlan> zhuanlanList = new ArrayList<>();
    private RecyclerView recyclerView;

    private Gson gson = new Gson();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new MyDataBaseHelper(getActivity(),"User_defined_IDs.db",null,1);
        db = dbHelper.getWritableDatabase();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_user_define,container,false);

        initViews(view);

        Cursor cursor = db.query("Ids",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do {
                list.add(cursor.getString(cursor.getColumnIndex("zhuanlanID")));
            } while (cursor.moveToNext());

        }
        cursor.close();

        if (list.size() == 0){
            tvUserDefine.setVisibility(View.VISIBLE);
        } else {

            tvUserDefine.setVisibility(View.GONE);

            refreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(true);
                }
            });

            for (int i = 0; i < list.size(); i++){

                final int finalI = i;

                StringRequest request = new StringRequest(Request.Method.GET, API.BASE_URL + list.get(i), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Zhuanlan z = gson.fromJson(s, Zhuanlan.class);
                        zhuanlanList.add(z);

                        if (adapter == null) {
                            adapter = new ZhuanlanAdapter(getActivity(), zhuanlanList);
                            recyclerView.setAdapter(adapter);
                            adapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
                                @Override
                                public void OnClick(View v, int position) {
                                    Intent intent = new Intent(getContext(),PostsListActivity.class);
                                    intent.putExtra("slug",zhuanlanList.get(position).getSlug());
                                    intent.putExtra("title",zhuanlanList.get(position).getName());
                                    startActivity(intent);
                                }
                            });
                        } else {
                            adapter.notifyItemInserted(zhuanlanList.size() - 1);
                        }

                        if (finalI == (list.size() - 1)){

                            refreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    refreshLayout.setRefreshing(false);
                                }
                            });

                            refreshLayout.setEnabled(false);

                            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                                @Override
                                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                    return false;
                                }

                                @Override
                                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                                    adapter.remove(viewHolder.getLayoutPosition());
                                }
                            });
                            helper.attachToRecyclerView(recyclerView);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });

                VolleySingleton.getVolleySingleton(getActivity()).addToRequestQueue(request);

            }


        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.add_zhuanlan_id)
                        .content(R.string.add_zhuanlan_id_description)
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
                        .input("", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                            }
                        }).build();

                dialog.setActionButton(DialogAction.NEGATIVE,R.string.cancel);
                dialog.setActionButton(DialogAction.POSITIVE,R.string.ok);
                dialog.setActionButton(DialogAction.NEUTRAL, R.string.zhuanlan_id_help);

                dialog.getActionButton(DialogAction.NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // 监听输入面板的情况，如果激活则隐藏
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm.isActive()) {
                            imm.hideSoftInputFromWindow(fab.getWindowToken(), 0);
                        }

                        String url = getString(R.string.add_zhuanlan_id_help);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);

                    }
                });

                dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String input = dialog.getInputEditText().getText().toString();

                        if (!input.isEmpty()){

                            StringRequest request = new StringRequest(Request.Method.GET, API.BASE_URL + input, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String s) {

                                    Zhuanlan z = gson.fromJson(s, Zhuanlan.class);

                                    if (z == null) {
                                        Snackbar.make(fab, R.string.add_zhuanlan_id_error,Snackbar.LENGTH_SHORT).show();
                                        return;
                                    }

                                    Boolean exists = false;

                                    Cursor cursor = db.query("Ids",null,null,null,null,null,null);
                                    if (cursor.moveToFirst()){
                                        do {
                                            if (z.getSlug().equals(String.valueOf(cursor.getString(cursor.getColumnIndex("zhuanlanID"))))){
                                                exists = true;
                                                break;
                                            }
                                        } while (cursor.moveToNext());
                                    }
                                    cursor.close();

                                    if (!exists){

                                        // 向数据库中插入数据
                                        ContentValues values = new ContentValues();
                                        values.put("zhuanlanID",input.toLowerCase());
                                        db.insert("Ids",null,values);

                                        values.clear();

                                        zhuanlanList.add(z);

                                        adapter = new ZhuanlanAdapter(getActivity(),zhuanlanList);
                                        recyclerView.setAdapter(adapter);
                                        adapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
                                            @Override
                                            public void OnClick(View v, int position) {
                                                Intent intent = new Intent(getContext(),PostsListActivity.class);
                                                intent.putExtra("slug",zhuanlanList.get(position).getSlug());
                                                intent.putExtra("title",zhuanlanList.get(position).getName());
                                                startActivity(intent);
                                            }

                                        });

                                        tvUserDefine.setVisibility(View.GONE);

                                        adapter.notifyItemInserted(zhuanlanList.size() - 1);
                                        // 具体的删除操作在touch helper中完成

                                        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                                            @Override
                                            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                                return false;
                                            }

                                            @Override
                                            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                                                adapter.remove(viewHolder.getAdapterPosition());
                                            }
                                        });
                                        helper.attachToRecyclerView(recyclerView);


                                    } else {
                                        Snackbar.make(fab, R.string.added,Snackbar.LENGTH_SHORT).show();
                                    }

                                    // 监听输入面板的情况，如果激活则隐藏
                                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (imm.isActive()) {
                                        imm.hideSoftInputFromWindow(fab.getWindowToken(), 0);
                                    }

                                    dialog.dismiss();

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Snackbar.make(fab, R.string.add_zhuanlan_id_error,Snackbar.LENGTH_SHORT).show();
                                }
                            });

                            VolleySingleton.getVolleySingleton(getActivity()).addToRequestQueue(request);

                        }
                    }
                });

                dialog.show();
            }
        });

        return view;
    }

    private void initViews(View view) {

        tvUserDefine = (TextView) view.findViewById(R.id.tv_user_define);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        //设置下拉刷新的按钮的颜色
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        //设置手指在屏幕上下拉多少距离开始刷新
        refreshLayout.setDistanceToTriggerSync(300);
        //设置下拉刷新按钮的背景颜色
        refreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        //设置下拉刷新按钮的大小
        refreshLayout.setSize(SwipeRefreshLayout.DEFAULT);

    }

    @Override
    public void onStop() {
        super.onStop();

        if (refreshLayout.isRefreshing()){
            refreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                }
            });
        }
    }
}
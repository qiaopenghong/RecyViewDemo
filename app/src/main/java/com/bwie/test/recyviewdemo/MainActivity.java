package com.bwie.test.recyviewdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.bwie.test.recyviewdemo.com.bwie.test.bean.Data;
import com.bwie.test.recyviewdemo.com.bwie.test.bean.DataBean;
import com.bwie.test.recyviewdemo.com.bwie.test.util.OkHttp;
import com.bwie.test.recyviewdemo.com.bwie.test.util.RecyclerViewClickListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Request;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyview;
    private SwipeRefreshLayout srl;
    private HomeAdapter mAdapter;
    private List<Data> mlist;
    private int page=1;
    private int lastVisibleItemPosition;
    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mlist =(List<Data>)msg.obj;
            recyview.setAdapter(mAdapter = new HomeAdapter(mlist));
        }
    };
    private LinearLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();//初始化控件
        initData(page);//初始化数据
    }

    private void initData(int page) {
        OkHttp.getAsync("http://japi.juhe.cn/joke/content/list.from?key=%20874ed931559ba07aade103eee279bb37%20&page=" + page + "&pagesize=10&sort=asc&time=1418745237", new OkHttp.DataCallBack() {
            @Override
            public void requestFailure(Request request, IOException e) {

            }
            @Override
            public void requestSuccess(String result) throws Exception {
                Gson gson = new Gson();
                DataBean dataBean = gson.fromJson(result, DataBean.class);
                List<Data> data = dataBean.result.data;
                Message msg = new Message();
                msg.obj = data;
                myHandler.sendMessage(msg);
            }
        });
    }

    private void initView() {
        recyview = (RecyclerView) findViewById(R.id.recyview);
        srl = (SwipeRefreshLayout) findViewById(R.id.srl);
        manager = new LinearLayoutManager(MainActivity.this);
        recyview.setLayoutManager(manager);
        srl.setColorSchemeColors(Color.RED, Color.BLUE);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mlist.clear();
                initData(page++);
                srl.setRefreshing(false);
            }
        });
        recyview.addOnItemTouchListener(new RecyclerViewClickListener(MainActivity.this, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
            @Override
            public void onItemLongClick(View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("确认删除吗？");
                builder.setTitle("提示");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mlist.remove(position);
                        mAdapter.notifyDataSetChanged();
                    }


                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }));
        recyview.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState== NumberPicker.OnScrollListener.SCROLL_STATE_IDLE){
                    lastVisibleItemPosition = manager.findLastVisibleItemPosition();
                    if(lastVisibleItemPosition==mlist.size()-1){
                        initData(page+=1);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
        private List<Data> mlist;
        public HomeAdapter(List<Data> mlist) {
            this.mlist = mlist;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    MainActivity.this).inflate(R.layout.item_home, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tv_context.setText(mlist.get(position).content);
            holder.tv_data.setText(mlist.get(position).updatetime);
        }

        @Override
        public int getItemCount() {
            return mlist.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_context;
            TextView tv_data;
            public MyViewHolder(View view) {
                super(view);
                tv_context = (TextView) view.findViewById(R.id.tv_content);
                tv_data = (TextView) view.findViewById(R.id.tv_data);
            }
        }
    }

}



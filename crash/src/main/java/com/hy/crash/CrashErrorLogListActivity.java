package com.hy.crash;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created time : 2018/11/21 14:36.
 *
 * @author HY
 */
public class CrashErrorLogListActivity extends AppCompatActivity {

    private boolean mHasTop;
    private LogAdapter mAdapter;
    private File currentPath;
    private RelativeLayout mLytReturn;
    private LinearLayout mLytEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_log_list);
        boolean isStatusBlack = CommonUtils.getTypeValueBoolean(this, R.attr.crash_light_toolbar);
        CommonUtils.processMIUI(this, isStatusBlack);

        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        String file = intent.getStringExtra("file");

        mLytReturn = findViewById(R.id.crash_lyt_return);
        View mLytBack = findViewById(R.id.crash_lyt_back);
        ListView lvList = findViewById(R.id.crash_lv_list);
        mLytEmpty = findViewById(R.id.crash_lyt_empty);


        mAdapter = new LogAdapter();
        lvList.setAdapter(mAdapter);

        mLytBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mLytReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasTop) {
                    initData(currentPath.getParentFile());
                }
            }
        });

        currentPath = new File(path);
        showFile(file);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData(currentPath);
    }


    private void showFile(String file) {
        startActivity(new Intent(this, CrashLogActivity.class)
                .putExtra("file", file));
    }

    private void initData(File filePath) {
        currentPath = filePath;
        mHasTop = !filePath.getName().equals("crash");
        mLytReturn.setVisibility(mHasTop ? View.VISIBLE : View.GONE);

        mFiles = filePath.listFiles();
        mLytEmpty.setVisibility(mFiles.length == 0 ? View.VISIBLE : View.GONE);
        mAdapter.notifyDataSetChanged();
    }


    private File[] mFiles;

    private void onItemClick(int position) {
        if (isDeleting) return;
        File file = mFiles[position];
        if (file.isDirectory()) {
            initData(file);
        } else {
            showFile(file.getAbsolutePath());
        }
    }

    private class LogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFiles == null ? 0 : mFiles.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                convertView = LayoutInflater.from(CrashErrorLogListActivity.this).inflate(R.layout.crash_item_log, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.bind(position);
            return convertView;
        }
    }

    private class ViewHolder {
        private ImageView mIvIcon;
        private TextView mTvTitle;
        private TextView mTvSize;

        private View itemView;

        ViewHolder(View itemView) {
            this.itemView = itemView;
            mIvIcon = itemView.findViewById(R.id.crash_iv_icon);
            mTvTitle = itemView.findViewById(R.id.crash_tv_title);
            mTvSize = itemView.findViewById(R.id.crash_tv_size);
        }

        void bind(final int position) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemLongClick(position);
                    return true;
                }
            });
            File file = mFiles[position];
            mIvIcon.setImageResource(file.isDirectory() ?
                    R.drawable.crash_ic_folder :
                    R.drawable.crash_file_text);
            mTvTitle.setText(file.getName());
            mTvSize.setVisibility(View.VISIBLE);
            mTvSize.setText(file.isDirectory() ? "<DIR>" : formatFileSize(file.length()));
        }
    }

    private void onItemLongClick(int position) {
        if (isDeleting) return;
        final File file = mFiles[position];

        String message = getString(R.string.crash_delete_message, file.isFile() ? getString(R.string.crash_str_file) : getString(R.string.crash_str_folder));
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.crash_str_dlg_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if (file.isFile()) {
                            boolean delete = file.delete();
                            if (delete) {
                                Toast.makeText(CrashErrorLogListActivity.this, getString(R.string.crash_delete_success), Toast.LENGTH_SHORT).show();
                                initData(currentPath);
                            }
                        } else {
                            delete(file);
                            isDeleting = false;
                            initData(currentPath);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private boolean isDeleting = false;

    private void delete(File path) {
        isDeleting = true;
        if (path.isDirectory()) {
            File[] files = path.listFiles();

            for (File file : files) {
                delete(file);
            }
        }

        //noinspection ResultOfMethodCallIgnored
        path.delete();
    }

    @NonNull
    public String formatFileSize(long fileSize) {
        if (fileSize > 0 && fileSize < 1023) {
            return fileSize + "B";
        } else if (fileSize >= 1024 && fileSize < 1024 * 1024) {
            return fileSize / 1024 + "KB";
        } else if (fileSize >= 1024 * 1024 && fileSize < 1024 * 1024 * 1024) {
            return fileSize / (1024 * 1024) + "MB";
        } else {
            return fileSize / (1024 * 1024 * 1024) + "GB";
        }
    }
}

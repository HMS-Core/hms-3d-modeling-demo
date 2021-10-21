package com.huawei.hms.modeling3d.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hms.modeling3d.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FileAdapter extends FileRecyclerAdapter<File> implements FileRecyclerAdapter.OnItemClickListener{

    private File mRootDir;
    private File mCurrentDir;
    private boolean[] mCheckedFlags;
    public OnDirChangeListener mOnDirChangeListener;

    private Comparator<File> mFileComparator = (file1, file2) -> {
        if (file1.isDirectory() && file2.isFile()) {
            return -1;
        }
        if (file1.isFile() && file2.isDirectory()) {
            return 1;
        }
        return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase());
    };

    private FilenameFilter mFilenameFilter = (dir, name) -> {
        if (name.startsWith(".")) {
            return false;
        }
        return true;
    };

    public FileAdapter(Context context, String rootPath) {
        mContext = context;
        mList = new ArrayList<>();
        mLayoutId = R.layout.item_file_list;
        setOnItemClickListener(this);

        mRootDir = new File(rootPath);
        mCurrentDir = mRootDir;
        updateList();
    }

    @Override
    public void convert(ViewHolder holder, File file, final int position) {
        ImageView fileIcon = holder.getView(R.id.item_file_icon);
        TextView filename = holder.getView(R.id.item_file_name);
        CheckBox checkBox = holder.getView(R.id.item_check_box);
        ImageView arrowIcon = holder.getView(R.id.item_arrow);

        ImageView ivClickBox = holder.getView(R.id.item_click_box);


        TextView tvHasChild = holder.getView(R.id.tv_has_child);

        if (file.isDirectory()) {
            ivClickBox.setVisibility(View.VISIBLE);
            tvHasChild.setVisibility(View.VISIBLE);
            tvHasChild.setText("" + file.list().length + mContext.getString(R.string.item_text));
        } else {
            ivClickBox.setVisibility(View.INVISIBLE);
            tvHasChild.setVisibility(View.INVISIBLE);
        }

        int resId = file.isDirectory() ? R.drawable.folder_icon : R.drawable.other_file_icon;
        fileIcon.setImageResource(resId);
        filename.setText(file.getName());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> mCheckedFlags[position] = isChecked);
        checkBox.setChecked(mCheckedFlags[position]);

        if (mCheckedFlags[position]){
            ivClickBox.setImageResource(R.drawable.select_rb_icon);
        }else {
            ivClickBox.setImageResource(R.drawable.unselect_rb_icon);
        }

        ivClickBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mCheckedFlags[position] = true;
//                singleSelect(position);
                if (mCheckedFlags[position]) {
                    mCheckedFlags[position] = false;
                }else {
                    mCheckedFlags[position] = true;
                }
                notifyDataSetChanged();
            }
        });

        if (file.isDirectory()) {
            arrowIcon.setVisibility(View.VISIBLE);
        } else {
            arrowIcon.setVisibility(View.GONE);
        }
    }

    public boolean isRootDir() {
        if (mRootDir == null || mCurrentDir == null) {
            return true;
        }
        return mRootDir.getAbsolutePath().equals(mCurrentDir.getAbsolutePath());
    }

    public void quitMode() {
        if (mCheckedFlags!=null) {
            Arrays.fill(mCheckedFlags, false);
            notifyDataSetChanged();
        }
    }

    public void singleSelect(int position){
        if (mCheckedFlags!=null) {
            Arrays.fill(mCheckedFlags, false);
            for (int i = 0 ;i<mCheckedFlags.length;i++){
                if (position==i){
                    mCheckedFlags[i]=true;
                }else {
                    mCheckedFlags[i]=false;
                }
            }
            notifyDataSetChanged();
        }
    }

    public void backParent() {
        if (isRootDir()) {
            return;
        }
        mCurrentDir = mCurrentDir.getParentFile();
        updateList();
        if (mOnDirChangeListener != null) {
            mOnDirChangeListener.onDirChangeListener(mCurrentDir);
        }
    }

    public ArrayList<String> getChoosePaths() {
        if (mCheckedFlags==null){
            return null;
        }
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < mCheckedFlags.length; i++) {
            if (mCheckedFlags[i]) {
                result.add(mList.get(i).getAbsolutePath());
            }
        }
        return result.size() > 0 ? result : null;
    }

    public void setOnDirChangeListener(OnDirChangeListener onDirChangeListener) {
        mOnDirChangeListener = onDirChangeListener;
    }

    @Override
    public void onItemClick(View view, int position) {
        File file = mList.get(position);
        if (file.isDirectory()) {
            mCurrentDir = new File(file.getAbsolutePath());
            updateList();
            if (mOnDirChangeListener != null) {
                mOnDirChangeListener.onDirChangeListener(mCurrentDir);
            }
        }
    }

    public void updateList() {
        File[] files = mCurrentDir.listFiles(mFilenameFilter);
        mList.clear();
        if (files != null && files.length > 0) {
            Arrays.sort(files, mFileComparator);
            mList.addAll(Arrays.asList(files));
            mCheckedFlags = new boolean[mList.size()];
        }
        notifyDataSetChanged();
    }


    public interface OnDirChangeListener {

        void onDirChangeListener(File currentDirectory);
    }

}

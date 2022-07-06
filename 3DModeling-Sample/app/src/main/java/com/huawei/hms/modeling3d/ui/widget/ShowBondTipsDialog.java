package com.huawei.hms.modeling3d.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.modeling3d.R;

public class ShowBondTipsDialog extends Dialog {

    Context mContext;
    ViewPager2 viewPager;
    Indicator indicator;

    public ShowBondTipsDialog(@NonNull Context context) {
        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.show_bond_tips_layout, null);
        initView(view);
        Window window = this.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.y = Utils.dip2px(mContext, 32);
            Display d = window.getWindowManager().getDefaultDisplay();
            Point point = new Point();
            d.getSize(point);
            lp.width = (int) (point.x * 0.8);
            ViewGroup.LayoutParams params = viewPager.getLayoutParams();
            params.width = (int) (point.x * 0.85);
            viewPager.setLayoutParams(params);
            window.setAttributes(lp);
        }
        setContentView(view);
    }

    private void initView(View view) {
        viewPager = view.findViewById(R.id.view_page);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);
        indicator = (Indicator) view.findViewById(R.id.indicator);
        indicator.setTotalIndex(5);

        view.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        indicator.setCurrentIndex(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                indicator.setCurrentIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {

        int[] photo = new int[]{R.drawable.frist, R.drawable.second, R.drawable.thrid, R.drawable.five, R.drawable.four};
        int[] tips = new int[]{R.string.first_text, R.string.second_text, R.string.thrid_text, R.string.four_text, R.string.five_text};
        int[] steps = new int[]{R.string.first_step_text, R.string.second_step_text, R.string.third_step_text, R.string.the_fourth_step_text, R.string.the_fifth_step_text};

        @NonNull
        @Override
        public ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewPagerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_page_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewPagerViewHolder holder, int position) {
            holder.tvTip.setText(getContext().getText(tips[position]));
            holder.tvStep.setText(getContext().getText(steps[position]));
            holder.mImageView.setImageResource(photo[position]);
        }

        @Override
        public int getItemCount() {
            return photo.length;
        }

        class ViewPagerViewHolder extends RecyclerView.ViewHolder {

            TextView tvStep;
            TextView tvTip;
            ImageView mImageView;

            public ViewPagerViewHolder(@NonNull View itemView) {
                super(itemView);
                mImageView = itemView.findViewById(R.id.iv_step);
                tvStep = itemView.findViewById(R.id.tv_step);
                tvTip = itemView.findViewById(R.id.tv_tip);
            }
        }

    }
}

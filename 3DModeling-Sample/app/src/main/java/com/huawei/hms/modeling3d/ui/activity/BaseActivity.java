package com.huawei.hms.modeling3d.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * 应用打包的时间点
     *
     * @return 返回应用到期时间戳
     */
    public static long getUseExpirationTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date date = null;
        try {
            date = sdf.parse("2022-06-05 20:05:00");
            assert date != null;
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
            if (getUseExpirationTime()-System.currentTimeMillis()<0){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("The application expires. For details, contact hms3dmodeling@huawei.com");
                builder.setCancelable(false);
                builder.create().show();
            }
    }
}


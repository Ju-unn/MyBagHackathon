package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;

/** A9 · WeatherIcon — Type = Sun / Rain / Cloud / Snow. 20x20dp circle + glyph. */
public class WeatherIconView extends FrameLayout {

    private FrameLayout circle;
    private ImageView glyph;

    public WeatherIconView(Context context) {
        super(context);
        init(context, null);
    }

    public WeatherIconView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WeatherIconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_weather_icon, this, true);
        circle = findViewById(R.id.weatherIconCircle);
        glyph = findViewById(R.id.weatherIconGlyph);

        int type = 0;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BagWeatherIconView);
            type = a.getInt(R.styleable.BagWeatherIconView_weatherType, 0);
            a.recycle();
        }
        setType(type);
    }

    public void setType(int type) {
        int colorRes;
        int iconRes;
        switch (type) {
            case 1:
                colorRes = R.color.bag_weather_rain;
                iconRes = R.drawable.ic_weather_rain;
                break;
            case 2:
                colorRes = R.color.bag_weather_cloud;
                iconRes = R.drawable.ic_weather_cloud;
                break;
            case 3:
                colorRes = R.color.bag_weather_cloud;
                iconRes = R.drawable.ic_weather_snow;
                break;
            default:
                colorRes = R.color.bag_weather_sun;
                iconRes = R.drawable.ic_weather_sun;
                break;
        }
        circle.getBackground().mutate().setTint(ContextCompat.getColor(getContext(), colorRes));
        glyph.setImageResource(iconRes);
    }
}

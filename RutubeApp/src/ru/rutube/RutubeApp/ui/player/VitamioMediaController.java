package ru.rutube.RutubeApp.ui.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;


import io.vov.vitamio.widget.MediaController;

/**
 * Created by oleg on 7/29/13.
 */
public class VitamioMediaController extends MediaController {

    private Context mContext;

    public VitamioMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public VitamioMediaController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected View makeControllerView() {
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(ru.rutube.RutubeApp.R.layout.mediacontroller, this);
    }
}

package ru.rutube.RutubeAPI.requests;

import android.content.Context;
import com.foxykeep.datadroid.requestmanager.RequestManager;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 21:29
 * To change this template use File | Settings | File Templates.
 */
public final class RutubeRequestManager extends RequestManager {
    private static RutubeRequestManager sInstance;

    public static RutubeRequestManager from(Context context) {
        if (sInstance == null) {
            sInstance = new RutubeRequestManager(context);
        }

        return sInstance;
    }
    private RutubeRequestManager(Context context) {
        super(context, RutubeRestService.class);
    }
}

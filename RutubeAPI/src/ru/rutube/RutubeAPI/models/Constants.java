package ru.rutube.RutubeAPI.models;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 21:14
 * To change this template use File | Settings | File Templates.
 */
public final class Constants {
    public final class Result {
        public static final String TRACKINFO = "ru.rutube.api.models.trackinfo";
        public static final String PER_PAGE = "ru.rutube.api.per_page";
        public static final String TOKEN = "ru.rutube.api.login.token";
        public static final String PROGRESS = "ru.rutube.api.upload.progress";
        public static final String AUTH_COOKIE = "ru.rutube.api.login.cookie";
    }

    public final class Params {
        public static final String UPLOAD_SESSION = "ru.rutube.api.upload.session_id";
        public static final String VIDEO_ID = "ru.rutube.models.video.id";
        public static final String TITLE = "ru.rutube.api.video.title";
        public static final String DESCRIPTION = "ru.rutube.api.video.description";
        public static final String HIDDEN = "ru.rutube.api.video.hidden";
        public static final String CATEGORY_ID = "ru.rutube.api.video.category_id";
        public static final String FEED_URI = "ru.rutube.uri.feed";
        public static final String CONTENT_URI = "ru.rutube.uri.content";
        public static final String PAGE = "ru.rutube.uri.page";
        public static final String EMAIL = "ru.rutube.api.login.email";
        public static final String PASSWORD = "ru.rutube.api.login.password";
        public static final String VIDEO_URI = "ru.rutube.api.upload.video";
    }

    public final class Actions {
        public static final String UPLOAD_PROGRESS = "ru.rutube.api.upload.progress";
        public static final String LOGIN_REQUIRED = "ru.rutube.api.login_required";
    }
}

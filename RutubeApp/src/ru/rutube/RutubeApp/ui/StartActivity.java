package ru.rutube.RutubeApp.ui;

import ru.rutube.RutubeApp.ui.feed.SGShowcaseFragment;
import ru.rutube.RutubeFeed.ui.ShowcaseActivity;
import ru.rutube.RutubeFeed.ui.ShowcaseFragment;

public class StartActivity extends ShowcaseActivity {

    @Override
    protected ShowcaseFragment initShowcaseFragment() {
        return new SGShowcaseFragment();
    }
}
package ru.rutube.RutubeApp.ui.feed;

import ru.rutube.RutubeApp.data.SGTabsAdapter;
import ru.rutube.RutubeFeed.ui.ShowcaseFragment;

/**
 * Created by tumbler on 15.03.14.
 */
public class SGShowcaseFragment extends ShowcaseFragment {
    @Override
    protected void initViewPagerAdapter() {
        mViewPagerAdapter = new SGTabsAdapter(getActivity(),
                getChildFragmentManager(), null);

    }
}

package ru.rutube.RutubeApp.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeApp.R;
import ru.rutube.RutubeApp.ctrl.MainPageController;
import ru.rutube.RutubeFeed.ui.FeedActivity;
import ru.rutube.RutubeFeed.ui.FeedFragment;

import java.util.HashMap;

public class StartActivity extends Activity implements MainPageController.MainPageView, ActionBar.TabListener {
    private static final String LOG_TAG = StartActivity.class.getName();
    private static final String CONTROLLER = "controller";
    private static final int LOGIN_REQUEST_CODE = 1;

    private MainPageController mController;
    private HashMap<String, ActionBar.Tab> mTabMap = new HashMap<String, ActionBar.Tab>();
    private HashMap<String, Fragment> mFragmentMap = new HashMap<String, Fragment>();
    private FragmentTransaction mFragmentTransaction;
    private Fragment mCurrentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mController = savedInstanceState.getParcelable(CONTROLLER);
        else
            mController = new MainPageController();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        mController.attach(this, this);
        initTabs();
    }

    /**
     * Проксирует обработку выбора вкладки в контроллер, попутно запоминая текущую транзакцию
     * фрагмента для использования в обратном вызове
     * @param tab тег вкладки
     * @param fragmentTransaction
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mFragmentTransaction = fragmentTransaction;
        String tag = (String)tab.getTag();
        mController.onTabSelected(tag);
        mFragmentTransaction = null;

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    /**
     * Делает активной вкладку с тегом
     * @param tag
     */
    public void selectTab(String tag) {
        getActionBar().selectTab(mTabMap.get(tag));
    }

    /**
     * Добавляет новую вкладку в таб-навигацию
     * @param title
     * @param tag
     */
    public void addFeedTab(String title, String tag) {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        ActionBar.Tab tab = actionBar.newTab();
        tab.setText(title);
        tab.setTabListener(this);
        tab.setTag(tag);
        actionBar.addTab(tab);
        mTabMap.put(tag, tab);
    }

    /**
     * Делает активным фрагмент ленты с определенным тегом
     * @param tag тег фрагмента
     * @param feedUri uri ленты
     */
    public void showFeedFragment(String tag, Uri feedUri) {
        // ищем фрагмент в локальном кэше
        Fragment fragment = mFragmentMap.get(tag);
        Boolean isNewFragment = false;
        // не нашли, конструируем новый фрагмент с feedUri
        if (fragment == null) {
            fragment = createFeedFragment(feedUri);
            // добавляем в кэш
            isNewFragment = true;
            mFragmentMap.put(tag, fragment);
        }

        // Транзакция может уже быть открыта, если метод вызывается в обработчике таб-навигации,
        if (mFragmentTransaction != null){
            // Скрываем старый фрагмент
            if (mCurrentFragment != null){
                mFragmentTransaction.hide(mCurrentFragment);
            }
            // Добавляем или показываем новый фрагмент
            if (isNewFragment)
            {
                mFragmentTransaction.add(R.id.feed_fragment_container, fragment);
            }
            else {
                mFragmentTransaction.show(fragment);
            }
        }
        else {
            // Заменяем заглушку на новый фрагмент
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.feed_fragment_container, fragment, tag);
            ft.commit();
        }
        mCurrentFragment = fragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CONTROLLER, mController);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            //processCurrentTab();
            // TODO: обработка процесса авторизации
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Настраивает таб-навигацию
     */
    private void initTabs() {
        Log.d(LOG_TAG, "initTabs");
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mController.initTabs();
        Log.d(LOG_TAG, "initTabs done");

    }

    /**
     * Конструирует новый фрагмент с лентой
     * @param feedUri uri ленты
     * @return готовый к использованию фрагмент ленты
     */
    private Fragment createFeedFragment(Uri feedUri) {
        Fragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.Params.FEED_URI, feedUri);
        fragment.setArguments(args);
        return fragment;
    }

    // TODO: обработка ссылок

//    @Override
//    public void onLoginResult(int result) {
//        if (result == RESULT_OK)
//            processCurrentTab();
//    }
}

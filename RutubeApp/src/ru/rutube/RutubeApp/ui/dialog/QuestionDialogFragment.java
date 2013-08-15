
package ru.rutube.RutubeApp.ui.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Класс создания диалога "Вопроса"
 *
 * Пимер использования
 *
 * QuestionDialogFragment.QuestionDialogFragmentBuilder questionDialogFragmentBuilder = new QuestionDialogFragment.QuestionDialogFragmentBuilder(this);
 * questionDialogFragmentBuilder
 *          .setMessage("Хотите новые плюшки ?")
 *          .setTitle("Вопрос")
 *          .setPositiveButton("ДА !", new DialogInterface.OnClickListener() {
 *              @Override
 *              public void onClick(DialogInterface dialogInterface, int i) {
 *                   Log.d(LOG_TAG, "Да, я хочу плюшки!!");
 *              }
 *          })
 *          .show();
 */
public final class QuestionDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        Builder b = new Builder(getActivity());
        b.setTitle(args.getString(BUNDLE_TITLE));
        b.setMessage(args.getString(BUNDLE_MESSAGE));
        b.setIcon(android.R.drawable.ic_dialog_alert);
        setCancelable(true);
        b.setPositiveButton(args.getString(BUNDLE_POSITIVE_BUTTON_TEXT), positiveOnClickListener);
        b.setNegativeButton(args.getString(BUNDLE_NEGATIVE_BUTTON_TEXT), negativeOnClickListener);
        return b.create();
    }

    public static class QuestionDialogFragmentBuilder {

        private FragmentActivity activity;
        private String title;
        private String message;
        private String positiveButtonText;
        private OnClickListener positiveButtonOnClickListener;
        private String negativeButtonText;
        private OnClickListener negativeButtonOnClickListener;

        public QuestionDialogFragmentBuilder(FragmentActivity activity) {
            this.activity = activity;

            positiveButtonText = activity.getString(android.R.string.yes);
            negativeButtonText = activity.getString(android.R.string.no);
        }

        public QuestionDialogFragmentBuilder setTitle(int resId) {
            title = activity.getString(resId);
            return this;
        }

        public QuestionDialogFragmentBuilder setTitle(String text) {
            title = text;
            return this;
        }

        public QuestionDialogFragmentBuilder setMessage(int resId) {
            message = activity.getString(resId);
            return this;
        }

        public QuestionDialogFragmentBuilder setMessage(String text) {
            message = text;
            return this;
        }

        public QuestionDialogFragmentBuilder setPositiveButton(int resId,
                                                               OnClickListener onClickListener) {
            return setPositiveButton(activity.getString(resId), onClickListener);
        }

        public QuestionDialogFragmentBuilder setPositiveButton(String text,
                                                               OnClickListener onClickListener) {
            positiveButtonText = text;
            positiveButtonOnClickListener = onClickListener;
            return this;
        }

        public QuestionDialogFragmentBuilder setNegativeButton(int resId,
                                                               OnClickListener onClickListener) {
            return setNegativeButton(activity.getString(resId), onClickListener);
        }

        public QuestionDialogFragmentBuilder setNegativeButton(String text,
                                                               OnClickListener onClickListener) {
            negativeButtonText = text;
            negativeButtonOnClickListener = onClickListener;
            return this;
        }

        public void show() {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Fragment prev = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);

            QuestionDialogFragment.newInstance(title, message, positiveButtonText,
                    positiveButtonOnClickListener, negativeButtonText,
                    negativeButtonOnClickListener).show(fragmentManager, FRAGMENT_TAG);
        }

    }

    public static void dismiss(FragmentActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment prev = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.commit();
    }

    private static QuestionDialogFragment newInstance(String title, String message,
                                                      String positiveButtonText, OnClickListener positiveOnClickListener,
                                                      String negativeButtonText, OnClickListener negativeOnClickListener) {
        QuestionDialogFragment dialogFragment = new QuestionDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_TITLE, title);
        args.putString(BUNDLE_MESSAGE, message);
        args.putString(BUNDLE_POSITIVE_BUTTON_TEXT, positiveButtonText);
        args.putString(BUNDLE_NEGATIVE_BUTTON_TEXT, negativeButtonText);
        dialogFragment.setArguments(args);

        dialogFragment.positiveOnClickListener = positiveOnClickListener;
        dialogFragment.negativeOnClickListener = negativeOnClickListener;
        return dialogFragment;
    }

    private static final String FRAGMENT_TAG = "QuestionDialogFragment";

    private static final String BUNDLE_TITLE = "title";
    private static final String BUNDLE_MESSAGE = "message";
    private static final String BUNDLE_POSITIVE_BUTTON_TEXT = "positiveButtonText";
    private static final String BUNDLE_NEGATIVE_BUTTON_TEXT = "negativeButtonText";

    private OnClickListener positiveOnClickListener;
    private OnClickListener negativeOnClickListener;
}

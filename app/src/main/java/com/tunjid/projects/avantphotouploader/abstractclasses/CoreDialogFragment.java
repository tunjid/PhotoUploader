package com.tunjid.projects.avantphotouploader.abstractclasses;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.helpers.Utils;
import com.tunjid.projects.avantphotouploader.services.AvantApi;

import java.io.File;

import rx.Observer;
import rx.Subscription;

/**
 * A simple {@link DialogFragment} subclass.
 */
@SuppressWarnings("unused")
public abstract class CoreDialogFragment extends DialogFragment
        implements
        Observer<Integer> {

    protected final static boolean SHOW_KEYBOARD = true;
    protected final static boolean HIDE_KEYBOARD = false;
    protected final static boolean STATE_CREATED = true;
    protected final static boolean STATE_RESUMED = false;

    protected Subscription apiSubscription;

    private AlertDialog errorAlertDialog;


    public CoreDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (apiSubscription != null) {
            apiSubscription.unsubscribe();
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }


    protected AvantApi getApi() {
        return ((CoreActivity) getActivity()).getApi();
    }

    /**
     * Convenience method for forwarding all start activity calls to this {@link CoreDialogFragment}
     * instance so any final callbacks that need to be made can be made.
     *
     * @param i The intent to start
     */
    @Override
    public void startActivity(Intent i) {
        super.startActivity(i);
    }

    /**
     * Generate the tag for the fragment in the ViewPager
     */

   /* public static String getFragmentTag(int index) {
        return "android:switcher:" + R.id.pager + ":" + index;
    }*/

    public void alignDialogToTop(Dialog dialog, boolean showKeyBoardOnEntry, boolean dialogState) {

        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.

        SharedPreferences sharedPreferences
                = getActivity().getSharedPreferences(Utils.PREFS, Context.MODE_PRIVATE);

        // Get items required to put dialog just under the ActionBar.

        int screenWidth = sharedPreferences.getInt(Utils.SCREEN_WIDTH, 720);
        int screenHeight = sharedPreferences.getInt(Utils.SCREEN_HEIGHT, 1280);

        Window window = dialog.getWindow();

        window.setLayout(screenWidth, WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams windowLayoutParams = window.getAttributes();

        if (showKeyBoardOnEntry) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        windowLayoutParams.y = -((screenHeight / 2) - 56);

        if (dialogState) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            windowLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        }
        window.setAttributes(windowLayoutParams);
    }

    public void widenDialog(Dialog dialog, boolean showKeyBoardOnEntry, boolean dialogState) {

        SharedPreferences sharedPreferences
                = getActivity().getSharedPreferences(Utils.PREFS, Context.MODE_PRIVATE);

        // Get items required to put dialog just under the ActionBar.

        int screenWidth = sharedPreferences.getInt(Utils.SCREEN_WIDTH, 720);

        Window window = dialog.getWindow();

        window.setLayout(screenWidth, WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams windowLayoutParams = window.getAttributes();

        if (showKeyBoardOnEntry) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        if (dialogState) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            windowLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        }
        window.setAttributes(windowLayoutParams);
    }

    public boolean deleteFile(String imagePath, boolean showToast) {

        File file = new File((imagePath));

        boolean deleteSuccess = file.delete();

        if (deleteSuccess && showToast) {
            Toast.makeText(getActivity(), R.string.image_capture_cancelled, Toast.LENGTH_SHORT).show();
        }

        return deleteSuccess;
    }

    public static String getFragmentTag(int index) {
        return "android:switcher:" + R.id.pager + ":" + index;
    }

    /**
     * Shows an error dialog when there is a network issue.
     */
    public void showErrorDialog() {

        if (errorAlertDialog == null || !errorAlertDialog.isShowing()) {

            try {

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle(R.string.network_error);

                // 3. Get the AlertDialog from create()

                errorAlertDialog = builder.create();

                errorAlertDialog.show();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(Integer apiCall) {
    }

}

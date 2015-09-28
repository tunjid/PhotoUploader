package com.tunjid.projects.avantphotouploader.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.activities.HomeActivity;
import com.tunjid.projects.avantphotouploader.helpers.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * Base class for bound service that makes API calls.
 */

public class AvantApi extends Service
        implements
        Observer<ParseFile> {

    private static final int NOTIFICATION_ID = 1;

    private int currentProgress;
    private String exifOrientation;
    private String fileType;
    private String imagePath;

    private final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
    private NotificationManager notificationManager;

    private final IBinder binder = new LocalBinder();

    public AvantApi() {
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void createParseFileAsync(final String imagePath, final String fileType, final String exifOrientation) {

        Toast.makeText(this, R.string.processing, Toast.LENGTH_SHORT).show();

        this.fileType = fileType;
        this.exifOrientation = exifOrientation;

        Observable.defer(new Func0<Observable<ParseFile>>() {
            @Override
            public Observable<ParseFile> call() {
                return Observable.just(createParseFile(imagePath, fileType));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(12, TimeUnit.SECONDS)
                .subscribe(this);
    }

    // NOTE: it is difficult to rotate a high resolution image locally without running into
    // OutOfMemoryExceptions. This is a task best suited to a server side implementation.

    // The Orientation from exif data is added to every filetype upload to facilitate this.

    public ParseFile createParseFile(String imagePath, String formType) {

        try {
            this.imagePath = imagePath;

            File file = new File(imagePath);
            FileInputStream fis = new FileInputStream(file);

            Bitmap bm = BitmapFactory.decodeStream(fis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] data = baos.toByteArray();

            return new ParseFile(formType + ".jpg", data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void uploadImageToParse(final ParseFile parseFile, final String fileType) {

        Toast.makeText(this, R.string.starting_upload, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Utils.GENERIC_FLAG, HomeActivity.UPLOADED_FILES_TAG);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setContentTitle(getString(R.string.uploading_file))
                .setProgress(100, 0, false)
                .setAutoCancel(false);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());


        // First upload the file, and save it in the background.
        parseFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {

                    final ParseUser currentUser = ParseUser.getCurrentUser();

                    if (currentUser != null) {

                        updateNotification(R.string.finishing_up);

                        // Create new class holding file and accociated metadata
                        final ParseObject formDataFile = new ParseObject(fileType);

                        formDataFile.put(Utils.ORIENTATION, exifOrientation);
                        formDataFile.put(Utils.FORM_UPLOAD, parseFile);
                        formDataFile.put(Utils.IMAGE_PATH, imagePath);
                        formDataFile.put(Utils.CUSTOMER_ID, currentUser.getUsername());

                        // Save the custom class in the background
                        formDataFile.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException fileSaveException) {

                                // If successfull save it to the user.
                                if (fileSaveException == null) {
                                    currentUser.put(fileType, formDataFile);
                                    currentUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException saveToUserException) {
                                            if (saveToUserException == null) {
                                                notificationBuilder
                                                        .setContentTitle(getString(R.string.upload_successful))
                                                        .setContentText(getString(R.string.application_review))
                                                        .setProgress(0, 0, false)
                                                        .setAutoCancel(true);
                                                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                                                stopForeground(false);
                                            }
                                            else {
                                                saveToUserException.printStackTrace();
                                                updateNotification(R.string.upload_failure);
                                                stopForeground(false);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                    else {
                        updateNotification(R.string.upload_failure);
                        stopForeground(false);
                    }

                }
                else {
                    e.printStackTrace();
                    updateNotification(R.string.upload_failure);
                    stopForeground(false);
                }
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer percentDone) {

                // This can be done better, but will suffice for this example
                if (percentDone % 30 == 0 && percentDone != currentProgress) {
                    notificationBuilder
                            .setContentText(percentDone + "% done")
                            .setProgress(100, percentDone, false)
                    ;
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                }

                currentProgress = percentDone;
            }
        });
    }

    private void updateNotification(int stringResource) {
        notificationBuilder
                .setContentText(getString(stringResource))
                .setProgress(0, 0, false)
                .setAutoCancel(true);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(ParseFile parseFile) {
        if (parseFile != null) {
            uploadImageToParse(parseFile, fileType);
        }
        else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * <p> Local Binder class that returns Binder interface for clients to bind to. </p>
     */
    public class LocalBinder extends Binder {
        /**
         * <p> gets single unique instance of the API to bind to. </p>
         */
        public AvantApi getService() {
            return AvantApi.this;
        }
    }

}

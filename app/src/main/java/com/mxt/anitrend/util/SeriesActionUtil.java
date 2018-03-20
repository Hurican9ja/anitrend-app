package com.mxt.anitrend.util;

import android.app.ProgressDialog;
import android.arch.lifecycle.Lifecycle;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.LifecycleListener;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.general.SeriesList_;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2018/01/05.
 * Media list action helper class is responsible for showing the correct dialog
 * for a given series
 */

public class SeriesActionUtil implements RetroCallback<MediaList>, LifecycleListener {

    private ProgressDialog progressDialog;
    private WidgetPresenter<MediaList> presenter;
    private FragmentActivity context;

    private Lifecycle lifecycle;

    private Media series;
    private MediaList mediaList;
    private MediaBase mediaBase;

    SeriesActionUtil(FragmentActivity context) {
        this.context = context;
        this.lifecycle = context.getLifecycle();
        presenter = new WidgetPresenter<>(context);
    }

    private void setModels(Media series, MediaList mediaList, MediaBase mediaBase) {
        this.series = series;
        this.mediaList = mediaList;
        this.mediaBase = mediaBase;
    }

    private void actionPicker() {
        if(series != null) {
            presenter.getParams().putLong(KeyUtils.arg_id, series.getId());
            presenter.requestData(series.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]) ?
                            KeyUtils.ANIME_LIST_ITEM_REQ : KeyUtils.MANGA_LIST_ITEM_REQ, context, this);
        }
        else if (mediaBase != null) {
            presenter.getParams().putLong(KeyUtils.arg_id, mediaBase.getId());
            presenter.requestData(mediaBase.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]) ?
                    KeyUtils.ANIME_LIST_ITEM_REQ : KeyUtils.MANGA_LIST_ITEM_REQ, context, this);
        }
        else {
            presenter.getParams().putLong(KeyUtils.arg_id, mediaList.getMediaId());
            presenter.requestData(mediaList.getAnime() != null ?
                    KeyUtils.ANIME_LIST_ITEM_REQ : KeyUtils.MANGA_LIST_ITEM_REQ, context, this);
        }
    }

    private void dismissProgress() {
        if(progressDialog != null)
            progressDialog.dismiss();
    }

    public void startSeriesAction() {
        progressDialog = NotifyUtil.createProgressDialog(context, R.string.text_checking_collection);
        progressDialog.show();
        actionPicker();
    }

    private boolean isNewEntry(long seriesId) {
        return presenter.getDatabase().getBoxStore(MediaList.class).query()
                .equal(SeriesList_.series_id, seriesId)
                .build().count() < 1;
    }

    private void showActionDialog() {
        try {
            if(series != null)
                SeriesDialogUtil.createSeriesManage(context, series, isNewEntry(series.getId()), SeriesUtil.getSeriesTitle(series, presenter.getLanguagePreference()));
            else if(mediaBase != null)
                SeriesDialogUtil.createSeriesManage(context, mediaBase, isNewEntry(mediaBase.getId()), SeriesUtil.getSeriesTitle(mediaBase, presenter.getLanguagePreference()));
            else
                SeriesDialogUtil.createSeriesManage(context, mediaList, isNewEntry(mediaList.getMediaId()), SeriesUtil.getSeriesTitle(mediaList, presenter.getLanguagePreference()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.toString(), e.getLocalizedMessage());
        }
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call     the origination requesting object
     * @param response the response from the network
     */
    @Override
    public void onResponse(@NonNull Call<MediaList> call, @NonNull Response<MediaList> response) {
        if (lifecycle != null && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            MediaList seriesStatus;
            if(response.isSuccessful() && (seriesStatus = response.body()) != null) {
                if(!TextUtils.isEmpty(seriesStatus.getStatus()))
                    presenter.getDatabase().getBoxStore(MediaList.class).put(seriesStatus);
                showActionDialog();
            } else {
                Log.e(this.toString(), ErrorUtil.getError(response));
                NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show();
            }
            dismissProgress();
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call      the origination requesting object
     * @param throwable contains information about the error
     */
    @Override
    public void onFailure(@NonNull Call<MediaList> call, @NonNull Throwable throwable) {
        if (lifecycle != null && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            dismissProgress();
            throwable.printStackTrace();
            NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Unregister any listeners from fragments or activities
     *
     * @param changeListener
     */
    @Override
    public void onPause(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {
        if(presenter != null)
            presenter.onPause(changeListener);
    }

    /**
     * Register any listeners from fragments or activities
     *
     * @param changeListener
     */
    @Override
    public void onResume(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {
        if(presenter != null)
            presenter.onResume(changeListener);
    }

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    @Override
    public void onDestroy() {
        if(progressDialog != null)
            progressDialog.dismiss();
        if(presenter != null)
            presenter.onDestroy();
    }

    public static class Builder {

        private Media series;
        private MediaList mediaList;
        private MediaBase mediaBase;
        private SeriesActionUtil seriesActionUtil;

        public Builder setModel(Media series) {
            this.series = series;
            return this;
        }

        public Builder setModel(MediaList mediaList) {
            this.mediaList = mediaList;
            return this;
        }

        public Builder setModel(MediaBase mediaBase) {
            this.mediaBase = mediaBase;
            return this;
        }

        public SeriesActionUtil build(FragmentActivity context) {
            seriesActionUtil = new SeriesActionUtil(context);
            seriesActionUtil.setModels(series, mediaList, mediaBase);
            return seriesActionUtil;
        }
    }
}
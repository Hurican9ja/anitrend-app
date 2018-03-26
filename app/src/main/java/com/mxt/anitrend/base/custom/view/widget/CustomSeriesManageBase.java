package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2018/01/20.
 * CustomSeriesManageBase for managing mediaLists
 */

public abstract class CustomSeriesManageBase extends RelativeLayout implements CustomView, View.OnClickListener, AdapterView.OnItemSelectedListener {

    protected MediaPresenter presenter;

    protected MediaList model;

    public CustomSeriesManageBase(Context context) {
        super(context);
        onInit();
    }

    public CustomSeriesManageBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public CustomSeriesManageBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomSeriesManageBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        presenter = new MediaPresenter(getContext());
    }

    public void setModel(MediaBase model, boolean isNewEntry) {
        if(isNewEntry) {
            this.model = new MediaList();
            this.model.setMedia(model);
        }
        bindFields();
        populateFields();
    }

    public void setModel(MediaList model, boolean isNewEntry) {
        if(isNewEntry) {
            this.model = new MediaList();
            this.model.setMedia(model.getMedia());
        }
        bindFields();
        populateFields();
    }

    public @NonNull
    MediaList getModel() {
        return model;
    }

    public Bundle getParam() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(false);
        queryContainer.putVariable(KeyUtils.arg_mediaId, model.getMediaId());
        queryContainer.putVariable(KeyUtils.arg_listStatus, model.getStatus());
        queryContainer.putVariable(KeyUtils.arg_listScore_raw, model.getScore());
        queryContainer.putVariable(KeyUtils.arg_listNotes, model.getNotes());
        queryContainer.putVariable(KeyUtils.arg_listPrivate, model.isHidden());
        queryContainer.putVariable(KeyUtils.arg_listPriority, model.getPriority());
        queryContainer.putVariable(KeyUtils.arg_listHiddenFromStatusLists, model.isHiddenFromStatusLists());

        // TODO: 2018/03/25 Check if custom lists are enabled and work some magic
        queryContainer.putVariable(KeyUtils.arg_listAdvancedScore, model.getAdvancedScores());
        queryContainer.putVariable(KeyUtils.arg_listCustom, model.getCustomLists());

        queryContainer.putVariable(KeyUtils.arg_listRepeat, model.getRepeat());
        queryContainer.putVariable(KeyUtils.arg_listProgress, model.getProgress());
        queryContainer.putVariable(KeyUtils.arg_listProgressVolumes, model.getProgressVolumes());

        Bundle bundle = new Bundle();
        bundle.putParcelable(KeyUtils.arg_graph_params, queryContainer);
        return bundle;
    }

    /**
     * save current views states into the model
     */
    public abstract void persistChanges();

    protected abstract void populateFields();

    protected abstract void bindFields();

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        if(presenter != null)
            presenter.onDestroy();
    }

    protected MediaBase getSeriesModel() {
        return model.getMedia();
    }
}

package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.NotificationAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Notification;
import com.mxt.anitrend.model.entity.base.NotificationBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.CommentActivity;
import com.mxt.anitrend.view.activity.detail.MessageActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.activity.detail.MediaActivity;
import java.util.List;
import java.util.Objects;

/**
 * Created by max on 2017/12/06.
 * NotificationFragment
 */

public class NotificationFragment extends FragmentBaseList<Notification, List<Notification>, BasePresenter> {

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColumnSize = R.integer.single_list_x1; setInflateMenu(R.menu.shared_menu);
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new NotificationAdapter(model, getContext());
        injectAdapter();

        //Testing notifications by forcing the notification dispatcher
        /*for (int i = 0; i < 3; i++)
            NotificationDispatcher.createNotification(getContext(), new ArrayList<>(model.subList(i, i + 1)));*/
        // NotificationDispatcher.createNotification(getContext(), new ArrayList<>(model.subList(5, 6)));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_mark_all).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_mark_all:
                if(model != null) {
                    Stream.of(model)
                            .filter(notification -> !notification.isRead())
                            .forEach(notification -> notification.setRead(true));
                    /*getPresenter().getDatabase()
                            .getBoxStore(NotificationBase.class)
                            .put(model);*/
                    if (mAdapter != null)
                        mAdapter.notifyDataSetChanged();
                } else
                    NotifyUtil.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChanged(@Nullable List<Notification> content) {
        onPostProcessed(content);
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        getViewModel().requestData(KeyUtils.USER_NOTIFICATION_REQ, getContext());
    }

    private void setReadItems(Notification data) {
        data.setRead(true);
        getPresenter().getDatabase()
                .getBoxStore(NotificationBase.class)
                .put(data);
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, Notification data) {
        Intent intent;
        setReadItems(data);
        if(target.getId() == R.id.notification_img && !Objects.equals(data.getType(), KeyUtils.AIRING)) {
            intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra(KeyUtils.arg_userName, data.getUser().getName());
            CompatUtil.startRevealAnim(getActivity(), target, intent);
        }
        else
            switch (data.getType()) {
                case KeyUtils.ACTIVITY_MESSAGE:
                    intent = new Intent(getActivity(), MessageActivity.class);
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.FOLLOWING:
                    intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra(KeyUtils.arg_id, data.getUser().getId());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.THREAD_COMMENT_MENTION:
                    DialogUtil.createMessage(getContext(), data.getUser().getName(), data.getContext());
                    break;
                case KeyUtils.THREAD_SUBSCRIBED:
                    DialogUtil.createMessage(getContext(), data.getUser().getName(), data.getContext());
                    break;
                case KeyUtils.THREAD_COMMENT_REPLY:
                    DialogUtil.createMessage(getContext(), data.getUser().getName(), data.getContext());
                    break;
                case KeyUtils.AIRING:
                    intent = new Intent(getActivity(), MediaActivity.class);
                    intent.putExtra(KeyUtils.arg_id, data.getMedia().getId());
                    intent.putExtra(KeyUtils.arg_mediaType, data.getMedia().getType());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.ACTIVITY_LIKE:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtils.arg_id, data.getActivity().getId());
                    intent.putExtra(KeyUtils.arg_model, data.getActivity());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.ACTIVITY_REPLY_LIKE:
                    intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra(KeyUtils.arg_id, data.getActivity().getId());
                    intent.putExtra(KeyUtils.arg_model, data.getActivity());
                    CompatUtil.startRevealAnim(getActivity(), target, intent);
                    break;
                case KeyUtils.THREAD_LIKE:
                    DialogUtil.createMessage(getContext(), data.getUser().getName(), data.getContext());
                    break;
                case KeyUtils.THREAD_COMMENT_LIKE:
                    DialogUtil.createMessage(getContext(), data.getUser().getName(), data.getContext());
                    break;
            }
    }


    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long click index
     */
    @Override
    public void onItemLongClick(View target, Notification data) {
        if(Objects.equals(data.getType(), KeyUtils.AIRING)) {
            setReadItems(data);
            if(getPresenter().getApplicationPref().isAuthenticated()) {
                seriesActionUtil = new SeriesActionUtil.Builder()
                        .setModel(data.getMedia()).build(getActivity());
                seriesActionUtil.startSeriesAction();
            } else
                NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
        }
    }
}

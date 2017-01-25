package de.symeda.sormas.app.event;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;

import java.util.List;

import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.app.backend.common.AdoDtoHelper.DtoGetInterface;
import de.symeda.sormas.app.backend.common.AdoDtoHelper.DtoPostInterface;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.contact.ContactDtoHelper;
import de.symeda.sormas.app.backend.event.EventDtoHelper;
import de.symeda.sormas.app.backend.user.User;
import de.symeda.sormas.app.contact.ContactsListFragment;
import de.symeda.sormas.app.rest.RetroProvider;
import de.symeda.sormas.app.util.Callback;
import retrofit2.Call;

/**
 * @see <a href="http://square.github.io/retrofit/">Retrofit</a>
 */
public class SyncEventsTask extends AsyncTask<Void, Void, Void> {

    public SyncEventsTask() {
    }

    @Override
    protected Void doInBackground(Void... params) {

        new EventDtoHelper().pullEntities(new DtoGetInterface<EventDto>() {
            @Override
            public Call<List<EventDto>> getAll(long since) {

                User user = ConfigProvider.getUser();
                if (user != null) {
                    Call<List<EventDto>> all = RetroProvider.getEventFacade().getAll(user.getUuid(), since);
                    return all;
                }
                return null;
            }
        }, DatabaseHelper.getEventDao());

        new EventDtoHelper().pushEntities(new DtoPostInterface<EventDto>() {
            @Override
            public Call<Integer> postAll(List<EventDto> dtos) {
                // TODO postAll should return the date&time the server used as modifiedDate
                return RetroProvider.getEventFacade().postAll(dtos);
            }
        }, DatabaseHelper.getEventDao());

        return null;
    }

    public static void syncEvents(final FragmentManager fragmentManager) {
        if (fragmentManager != null) {
            syncEvents(new Callback() {
                @Override
                public void call() {
                    if (fragmentManager.getFragments() != null) {
                        for (Fragment fragement : fragmentManager.getFragments()) {
                            if (fragement instanceof EventsListFragment) {
                                fragement.onResume();
                            }
                        }
                    }
                }
            });
        } else {
            syncEvents((Callback)null);
        }
    }

    public static void syncEvents(final FragmentManager fragmentManager, SwipeRefreshLayout refreshLayout) {
        syncEvents(fragmentManager);
        refreshLayout.setRefreshing(false);
    }


    public static void syncEvents(final Callback callback) {
        new SyncEventsTask() {
            @Override
            protected void onPostExecute(Void aVoid) {
                if (callback != null) {
                    callback.call();
                }
            }
        }.execute();
    }
}
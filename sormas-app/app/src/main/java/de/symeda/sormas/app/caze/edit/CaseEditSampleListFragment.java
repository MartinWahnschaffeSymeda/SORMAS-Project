package de.symeda.sormas.app.caze.edit;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.app.BaseEditActivityFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.sample.Sample;
import de.symeda.sormas.app.core.BoolResult;
import de.symeda.sormas.app.core.IActivityCommunicator;
import de.symeda.sormas.app.core.adapter.databinding.OnListItemClickListener;
import de.symeda.sormas.app.core.async.IJobDefinition;
import de.symeda.sormas.app.core.async.ITaskExecutor;
import de.symeda.sormas.app.core.async.ITaskResultCallback;
import de.symeda.sormas.app.core.async.ITaskResultHolderIterator;
import de.symeda.sormas.app.core.async.TaskExecutorFor;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.databinding.FragmentFormListLayoutBinding;
import de.symeda.sormas.app.rest.SynchronizeDataAsync;
import de.symeda.sormas.app.sample.edit.SampleEditActivity;
import de.symeda.sormas.app.shared.CaseFormNavigationCapsule;
import de.symeda.sormas.app.shared.SampleFormNavigationCapsule;
import de.symeda.sormas.app.util.SampleHelper;

/**
 * Created by Orson on 16/02/2018.
 * <p>
 * www.technologyboard.org
 * sampson.orson@gmail.com
 * sampson.orson@technologyboard.org
 */

public class CaseEditSampleListFragment extends BaseEditActivityFragment<FragmentFormListLayoutBinding, List<Sample>, Case> implements OnListItemClickListener {

    private AsyncTask onResumeTask;
    private String recordUuid;
    private InvestigationStatus pageStatus = null;
    private List<Sample> record;
    private CaseEditSampleListAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        savePageStatusState(outState, pageStatus);
        saveRecordUuidState(outState, recordUuid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = (savedInstanceState != null)? savedInstanceState : getArguments();

        recordUuid = getRecordUuidArg(arguments);
        pageStatus = (InvestigationStatus) getPageStatusArg(arguments);
    }

    @Override
    protected String getSubHeadingTitle() {
        Resources r = getResources();
        return r.getString(R.string.caption_case_samples);
    }

    @Override
    public List<Sample> getPrimaryData() {
        return record;
    }

    @Override
    public boolean onBeforeLayoutBinding(Bundle savedInstanceState, TaskResultHolder resultHolder, BoolResult resultStatus, boolean executionComplete) {
        if (!executionComplete) {
            Case caze = getActivityRootData();
            List<Sample> sampleListList = new ArrayList<Sample>();

            //Case caze = DatabaseHelper.getCaseDao().queryUuidReference(recordUuid);
            if (caze != null) {
                if (caze.isUnreadOrChildUnread())
                    DatabaseHelper.getCaseDao().markAsRead(caze);

                if (caze.getPerson() == null) {
                    caze.setPerson(DatabaseHelper.getPersonDao().build());
                }

                sampleListList = DatabaseHelper.getSampleDao().queryByCase(caze);
            }

            resultHolder.forList().add(sampleListList);
        } else {
            ITaskResultHolderIterator listIterator = resultHolder.forList().iterator();
            if (listIterator.hasNext()) {
                record = listIterator.next();

            }
        }

        return true;
    }

    @Override
    public void onLayoutBinding(FragmentFormListLayoutBinding contentBinding) {
        showEmptyListHintWithAdd(record, R.string.entity_sample);

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        adapter = new CaseEditSampleListAdapter(this.getActivity(), R.layout.row_edit_sample_list_item_layout, this, record);
        contentBinding.recyclerViewForList.setLayoutManager(linearLayoutManager);
        contentBinding.recyclerViewForList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAfterLayoutBinding(FragmentFormListLayoutBinding contentBinding) {

    }

    @Override
    protected void updateUI(FragmentFormListLayoutBinding contentBinding, List<Sample> samples) {

    }

    @Override
    public void onPageResume(FragmentFormListLayoutBinding contentBinding, boolean hasBeforeLayoutBindingAsyncReturn) {
        final SwipeRefreshLayout swiperefresh = (SwipeRefreshLayout)this.getView().findViewById(R.id.swiperefresh);
        if (swiperefresh != null) {
            swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getActivityCommunicator().synchronizeData(SynchronizeDataAsync.SyncMode.Changes, true, false, true, swiperefresh, null);
                }
            });
        }

        if (!hasBeforeLayoutBindingAsyncReturn)
            return;

        ITaskExecutor executor = TaskExecutorFor.job(new IJobDefinition() {
            @Override
            public void preExecute(BoolResult resultStatus, TaskResultHolder resultHolder) {
                //getActivityCommunicator().showPreloader();
                //getActivityCommunicator().hideFragmentView();
            }

            @Override
            public void execute(BoolResult resultStatus, TaskResultHolder resultHolder) {
                Case caze = getActivityRootData();
                List<Sample> sampleListList = new ArrayList<Sample>();

                //Case caze = DatabaseHelper.getCaseDao().queryUuidReference(recordUuid);
                if (caze != null) {
                    if (caze.isUnreadOrChildUnread())
                        DatabaseHelper.getCaseDao().markAsRead(caze);

                    if (caze.getPerson() == null) {
                        caze.setPerson(DatabaseHelper.getPersonDao().build());
                    }

                    sampleListList = DatabaseHelper.getSampleDao().queryByCase(caze);
                }

                resultHolder.forList().add(sampleListList);
            }
        });
        onResumeTask = executor.execute(new ITaskResultCallback() {
            @Override
            public void taskResult(BoolResult resultStatus, TaskResultHolder resultHolder) {
                //getActivityCommunicator().hidePreloader();
                //getActivityCommunicator().showFragmentView();

                if (resultHolder == null){
                    return;
                }

                ITaskResultHolderIterator listIterator = resultHolder.forList().iterator();

                if (listIterator.hasNext())
                    record = listIterator.next();

                if (record != null)
                    requestLayoutRebind();
                else {
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public int getRootEditLayout() {
        return R.layout.fragment_root_list_form_layout;
    }

    @Override
    public int getEditLayout() {
        return R.layout.fragment_form_list_layout;
    }

    @Override
    public boolean includeFabNonOverlapPadding() {
        return false;
    }

    @Override
    public void onListItemClick(View view, int position, Object item) {
        Sample s = (Sample)item;
        SampleFormNavigationCapsule dataCapsule = new SampleFormNavigationCapsule(getContext(), s.getUuid(), SampleHelper.getShipmentStatus(s));
        SampleEditActivity.goToActivity(getActivity(), dataCapsule);

        /*Sample s = (Sample)item;
        SampleFormNavigationCapsule dataCapsule = new SampleFormNavigationCapsule(getContext(),
                s.getUuid(), SampleHelper.getShipmentStatus(s));
        CaseEditSampleInfoActivity.goToActivity(getActivity(), dataCapsule);*/
    }

    @Override
    public boolean showSaveAction() {
        return false;
    }

    @Override
    public boolean showAddAction() {
        return true;
    }

    public static CaseEditSampleListFragment newInstance(IActivityCommunicator activityCommunicator, CaseFormNavigationCapsule capsule, Case activityRootData) {
        return newInstance(activityCommunicator, CaseEditSampleListFragment.class, capsule, activityRootData);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (onResumeTask != null && !onResumeTask.isCancelled())
            onResumeTask.cancel(true);
    }
}
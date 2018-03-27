package de.symeda.sormas.app.sample.read;

import android.databinding.ObservableArrayList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sample.SpecimenCondition;
import de.symeda.sormas.app.BaseReadActivityFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.sample.Sample;
import de.symeda.sormas.app.backend.sample.SampleTest;
import de.symeda.sormas.app.core.BoolResult;
import de.symeda.sormas.app.core.IActivityCommunicator;
import de.symeda.sormas.app.core.IEntryItemOnClickListener;
import de.symeda.sormas.app.core.async.IJobDefinition;
import de.symeda.sormas.app.core.async.ITaskExecutor;
import de.symeda.sormas.app.core.async.ITaskResultCallback;
import de.symeda.sormas.app.core.async.ITaskResultHolderIterator;
import de.symeda.sormas.app.core.async.TaskExecutorFor;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.databinding.FragmentSampleReadLayoutBinding;
import de.symeda.sormas.app.shared.SampleFormNavigationCapsule;
import de.symeda.sormas.app.shared.ShipmentStatus;

/**
 * Created by Orson on 11/12/2017.
 */

public class SampleReadFragment extends BaseReadActivityFragment<FragmentSampleReadLayoutBinding, Sample> {

    private AsyncTask onResumeTask;
    private String recordUuid = null;
    private ShipmentStatus pageStatus = null;
    private Sample record;
    private SampleTest mostRecentTest;

    private IEntryItemOnClickListener onRecentTestItemClickListener;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        SavePageStatusState(outState, pageStatus);
        SaveRecordUuidState(outState, recordUuid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = (savedInstanceState != null)? savedInstanceState : getArguments();

        recordUuid = getRecordUuidArg(arguments);
        pageStatus = (ShipmentStatus) getPageStatusArg(arguments);
    }

    @Override
    public boolean onBeforeLayoutBinding(Bundle savedInstanceState, TaskResultHolder resultHolder, BoolResult resultStatus, boolean executionComplete) {
        if (!executionComplete) {
            Sample s = DatabaseHelper.getSampleDao().queryUuid(recordUuid);
            resultHolder.forItem().add(s);
            resultHolder.forItem().add(DatabaseHelper.getSampleTestDao().queryMostRecentBySample(s));
        } else {
            ITaskResultHolderIterator itemIterator = resultHolder.forItem().iterator();

            //Item Data
            if (itemIterator.hasNext())
                record = itemIterator.next();

            if (itemIterator.hasNext())
                mostRecentTest = itemIterator.next();

            setupCallback();
        }

        return true;
    }

    @Override
    public void onLayoutBinding(FragmentSampleReadLayoutBinding contentBinding) {
        if (record == null)
            return;


        contentBinding.txtShipmentDetails.setVisibility((record.isShipped())? View.VISIBLE : View.GONE);
        contentBinding.txtOtherSample.setVisibility((record.getSampleMaterial() == SampleMaterial.OTHER)? View.VISIBLE : View.GONE);
        contentBinding.txtSampleSource.setVisibility((record.getAssociatedCase().getDisease() == Disease.AVIAN_INFLUENCA)? View.VISIBLE : View.GONE);
        contentBinding.sampleReceivedLayout.setVisibility((record.isReceived())? View.VISIBLE : View.GONE);

        if (record.getSpecimenCondition() != SpecimenCondition.NOT_ADEQUATE) {
            contentBinding.recentTestLayout.setVisibility(View.VISIBLE);
            if (mostRecentTest != null) {
                contentBinding.txtTestType.setVisibility(View.VISIBLE);
                //contentBinding.sampleTestResult.setVisibility(View.VISIBLE);
            }

            //contentBinding.txtTestType.setVisibility((mostRecentTest != null) ? View.VISIBLE : View.GONE);
            contentBinding.sampleNoRecentTestText.setVisibility((mostRecentTest == null) ? View.VISIBLE : View.GONE);
        }

        // only show referred to field when there is a referred sample
        if (record.getReferredTo() != null) {
            final Sample referredSample = record.getReferredTo();
            contentBinding.txtReferredTo.setVisibility(View.VISIBLE);
            contentBinding.txtReferredTo.setValue(getActivity().getResources().getString(R.string.sample_referred_to) + " " + referredSample.getLab().toString() + " " + "\u279D");
        } else {
            contentBinding.txtReferredTo.setVisibility(View.GONE);
        }

        contentBinding.setSample(record);
        contentBinding.setCaze(record.getAssociatedCase());
        contentBinding.setLab(record.getLab());

        contentBinding.setResults(getTestResults());
        contentBinding.setRecentTestItemClickCallback(onRecentTestItemClickListener);
    }

    @Override
    public void onAfterLayoutBinding(FragmentSampleReadLayoutBinding contentBinding) {

    }

    @Override
    public void onPageResume(FragmentSampleReadLayoutBinding contentBinding, boolean hasBeforeLayoutBindingAsyncReturn) {
        if (!hasBeforeLayoutBindingAsyncReturn)
            return;

        try {
            ITaskExecutor executor = TaskExecutorFor.job(new IJobDefinition() {
                @Override
                public void preExecute(BoolResult resultStatus, TaskResultHolder resultHolder) {
                    //getActivityCommunicator().showPreloader();
                    //getActivityCommunicator().hideFragmentView();
                }

                @Override
                public void execute(BoolResult resultStatus, TaskResultHolder resultHolder) {
                    if (recordUuid != null && !recordUuid.isEmpty()) {
                        Sample s = DatabaseHelper.getSampleDao().queryUuid(recordUuid);
                        resultHolder.forItem().add(s);
                        resultHolder.forItem().add(DatabaseHelper.getSampleTestDao().queryMostRecentBySample(s));
                    } else {
                        resultHolder.forItem().add(null);
                        resultHolder.forItem().add(null);
                    }

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

                    ITaskResultHolderIterator itemIterator = resultHolder.forItem().iterator();

                    if (itemIterator.hasNext())
                        record = itemIterator.next();

                    if (itemIterator.hasNext())
                        mostRecentTest = itemIterator.next();

                    if (record != null)
                        requestLayoutRebind();
                    else {
                        getActivity().finish();
                    }
                }
            });
        } catch (Exception ex) {
            //getActivityCommunicator().hidePreloader();
            //getActivityCommunicator().showFragmentView();
        }
    }

    @Override
    protected String getSubHeadingTitle() {
        String title = "";

        if (pageStatus != null) {
            title = pageStatus.toString();
        }

        return title;
    }

    @Override
    public Sample getPrimaryData() {
        return getContentBinding().getSample();
    }

    @Override
    public int getReadLayout() {
        return R.layout.fragment_sample_read_layout;
    }

    private ObservableArrayList getTestResults() {
        ObservableArrayList results = new ObservableArrayList();

        if (mostRecentTest != null)
            results.add(mostRecentTest);

        return results;
    }

    private void setupCallback() {
        onRecentTestItemClickListener = new IEntryItemOnClickListener() {
            @Override
            public void onClick(View v, Object item) {

            }
        };
    }

    @Override
    public boolean includeFabNonOverlapPadding() {
        return false;
    }

    public static SampleReadFragment newInstance(IActivityCommunicator activityCommunicator, SampleFormNavigationCapsule capsule)
            throws java.lang.InstantiationException, IllegalAccessException {
        return newInstance(activityCommunicator, SampleReadFragment.class, capsule);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (onResumeTask != null && !onResumeTask.isCancelled())
            onResumeTask.cancel(true);
    }
}
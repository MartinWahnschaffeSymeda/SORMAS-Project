package de.symeda.sormas.app.caze.edit;

import android.app.AlertDialog;
import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import de.symeda.sormas.app.BR;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.epidata.EpiDataGathering;
import de.symeda.sormas.app.backend.location.Location;
import de.symeda.sormas.app.component.TeboButtonType;
import de.symeda.sormas.app.component.dialog.BaseTeboAlertDialog;
import de.symeda.sormas.app.component.dialog.LocationDialog;
import de.symeda.sormas.app.component.dialog.TeboAlertDialogInterface;
import de.symeda.sormas.app.core.ICallback;
import de.symeda.sormas.app.core.IEntryItemOnClickListener;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.databinding.DialogEpidSocialEventsLayoutBinding;
import de.symeda.sormas.app.util.MemoryDatabaseHelper;

/**
 * Created by Orson on 19/02/2018.
 * <p>
 * www.technologyboard.org
 * sampson.orson@gmail.com
 * sampson.orson@technologyboard.org
 */

public class EpiDataGatheringDialog extends BaseTeboAlertDialog {

    public static final String TAG = EpiDataGatheringDialog.class.getSimpleName();

    private EpiDataGathering data;
    private IEntryItemOnClickListener onAddressLinkClickedCallback;
    private DialogEpidSocialEventsLayoutBinding mContentBinding;


    public EpiDataGatheringDialog(final FragmentActivity activity, EpiDataGathering epiDataGathering) {
        this(activity, R.string.heading_sub_case_epid_social_events, -1, epiDataGathering);
    }

    public EpiDataGatheringDialog(final FragmentActivity activity, int headingResId, int subHeadingResId, EpiDataGathering epiDataGathering) {
        super(activity, R.layout.dialog_root_layout, R.layout.dialog_epid_social_events_layout,
                R.layout.dialog_root_three_button_panel_layout, headingResId, subHeadingResId);

        this.data = epiDataGathering;

        setupCallback();
    }

    @Override
    protected void onOkClicked(View v, Object item, View rootView, ViewDataBinding contentBinding, ICallback callback) {
        /*DialogEpiDataGatheringLayoutBinding _contentBinding = (DialogEpiDataGatheringLayoutBinding)contentBinding;

        _contentBinding.spnState.enableErrorState("Hello");*/
        dismiss();
    }

    @Override
    protected void onDismissClicked(View v, Object item, View rootView, ViewDataBinding contentBinding, ICallback callback) {

    }

    @Override
    protected void onDeleteClicked(View v, Object item, View rootView, ViewDataBinding contentBinding, ICallback callback) {

    }

    @Override
    protected void recieveViewDataBinding(Context context, ViewDataBinding binding) {
        this.mContentBinding = (DialogEpidSocialEventsLayoutBinding)binding;
    }

    @Override
    protected void setBindingVariable(Context context, ViewDataBinding binding, String layoutName) {
        if (!binding.setVariable(BR.data, data)) {
            Log.e(TAG, "There is no variable 'data' in layout " + layoutName);
        }

        if (!binding.setVariable(BR.addressLinkCallback, onAddressLinkClickedCallback)) {
            Log.e(TAG, "There is no variable 'addressLinkCallback' in layout " + layoutName);
        }
    }

    @Override
    protected void initializeData(TaskResultHolder resultHolder, boolean executionComplete) {

    }

    @Override
    protected void initializeContentView(ViewDataBinding rootBinding, ViewDataBinding contentBinding, ViewDataBinding buttonPanelBinding) {
        //DialogEpidSocialEventsLayoutBinding _contentBinding = (DialogEpidSocialEventsLayoutBinding)contentBinding;

        mContentBinding.dtpDateOfEvent.initialize(getFragmentManager());
    }

    @Override
    public boolean isOkButtonVisible() {
        return true;
    }

    @Override
    public boolean isDismissButtonVisible() {
        return true;
    }

    @Override
    public boolean isDeleteButtonVisible() {
        return true;
    }

    @Override
    public boolean isRounded() {
        return true;
    }

    @Override
    public TeboButtonType dismissButtonType() {
        return TeboButtonType.BTN_LINE_SECONDARY;
    }

    @Override
    public TeboButtonType okButtonType() {
        return TeboButtonType.BTN_LINE_PRIMARY;
    }

    @Override
    public TeboButtonType deleteButtonType() {
        return TeboButtonType.BTN_LINE_DANGER;
    }

    private void setupCallback() {
        onAddressLinkClickedCallback = new IEntryItemOnClickListener() {
            @Override
            public void onClick(View v, Object item) {
                final Location location = MemoryDatabaseHelper.LOCATION.getLocations(1).get(0);
                final LocationDialog locationDialog = new LocationDialog(getActivity(), location);
                locationDialog.show(new ICallback<AlertDialog>() {
                    @Override
                    public void result(AlertDialog result) {

                    }
                });


                locationDialog.setOnPositiveClickListener(new TeboAlertDialogInterface.PositiveOnClickListener() {
                    @Override
                    public void onOkClick(View v, Object item, View viewRoot) {
                        /*getContentBinding().txtAddress.setValue(location.toString());
                        locationDialog.dismiss();*/
                    }
                });
            }
        };
    }

}
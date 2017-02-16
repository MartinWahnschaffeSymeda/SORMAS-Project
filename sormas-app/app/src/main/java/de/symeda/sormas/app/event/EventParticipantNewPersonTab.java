package de.symeda.sormas.app.event;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.contact.ContactProximity;
import de.symeda.sormas.api.contact.ContactRelation;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.contact.Contact;
import de.symeda.sormas.app.backend.event.EventParticipant;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.component.FieldHelper;
import de.symeda.sormas.app.component.RadioGroupField;
import de.symeda.sormas.app.databinding.ContactNewFragmentLayoutBinding;
import de.symeda.sormas.app.databinding.EventParticipantNewFragmentLayoutBinding;
import de.symeda.sormas.app.databinding.PersonSelectOrCreateFragmentLayoutBinding;
import de.symeda.sormas.app.person.PersonSelectVO;
import de.symeda.sormas.app.util.Callback;
import de.symeda.sormas.app.util.DataUtils;
import de.symeda.sormas.app.util.FormTab;
import de.symeda.sormas.app.util.Item;

/**
 * Created by Stefan Szczesny on 02.11.2016.
 */
public class EventParticipantNewPersonTab extends FormTab {

    private EventParticipant eventParticipant;
    private EventParticipantNewFragmentLayoutBinding binding;

    private Person selectedPersonFromDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        try {
            Person person = DataUtils.createNew(Person.class);
            eventParticipant = DataUtils.createNew(EventParticipant.class);
            eventParticipant.setPerson(person);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.event_participant_new_fragment_layout, container, false);
        return binding.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();

        binding.setEventParticipant(eventParticipant);

    }

    /**
     * Open the dialog for select a person from db filled list or create a new person.
     * Saving brings a refreshed person-reference.
     * @param person
     */
    public void selectOrCreatePersonDialog(final Person person, List<Person> existingPersons, final Callback callback) {
        try {
            final PersonSelectVO personSelectVO = new PersonSelectVO(person);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setTitle(getResources().getString(R.string.headline_pick_person));

            final PersonSelectOrCreateFragmentLayoutBinding bindingPersonSelect = DataBindingUtil.inflate(getLayoutInflater(null), R.layout.person_select_or_create_fragment_layout, null,false);
            bindingPersonSelect.setVo(personSelectVO);
            final View dialogView = bindingPersonSelect.getRoot();
            dialogBuilder.setView(dialogView);

            Button searchBtn = (Button) dialogView.findViewById(R.id.personSelect_search);
            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Person> existingPersons = null;
                    try {
                        // update person name from the fields
                        person.setFirstName(bindingPersonSelect.personFirstName.getValue());
                        person.setLastName(bindingPersonSelect.personLastName.getValue());

                        // search for existing person with this name
                        existingPersons = DatabaseHelper.getPersonDao().getAllByName(person.getFirstName(), person.getLastName());
                    } catch (SQLException e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    updatePersonSelectRadioGroupField(person, existingPersons, dialogView);
                }
            });


            updatePersonSelectRadioGroupField(person, existingPersons, dialogView);


            dialogBuilder.setPositiveButton(getResources().getString(R.string.action_select_person), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    selectedPersonFromDialog = (Person) bindingPersonSelect.personSelectSelectedPerson.getValue();
                    callback.call();
                }
            });
//            dialogBuilder.setNegativeButton(getResources().getString(R.string.action_dimiss), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int id) {
//                }
//            });

            AlertDialog newPersonDialog = dialogBuilder.create();
            newPersonDialog.show();


        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void updatePersonSelectRadioGroupField(Person person, List<Person> existingPersons, View dialogView) {
        List<Item> items = new ArrayList<Item>();
        for (Person existingPerson: existingPersons) {
            StringBuilder sb = new StringBuilder();
            sb.append(existingPerson.toString());
            sb.append(existingPerson.getSex()!=null || existingPerson.getApproximateAge()!=null || existingPerson.getPresentCondition()!=null ? "<br />":"");
            sb.append(existingPerson.getSex()!=null ? existingPerson.getSex():"");
            sb.append(existingPerson.getApproximateAge()!=null ? " | " + existingPerson.getApproximateAge() + " " +existingPerson.getApproximateAgeType():"");
            sb.append(existingPerson.getPresentCondition()!=null ? " | " +  existingPerson.getPresentCondition():"");
            items.add(new Item<Person>(Html.fromHtml(sb.toString()).toString(),existingPerson));
        }
        Item newPerson = new Item<Person>(getResources().getString(R.string.headline_new_person),person);
        items.add(0,newPerson);

        final RadioGroupField radioGroupField = (RadioGroupField) dialogView.findViewById(R.id.personSelect_selectedPerson);
        radioGroupField.removeAllItems();
        for(Item item : items) {
            radioGroupField.addItem(item);
        }

        // default select new person
        radioGroupField.setValue(person);
    }

    public Person getSelectedPersonFromDialog() {
        return selectedPersonFromDialog;
    }

    @Override
    public EventParticipant getData() {
        return binding.getEventParticipant();
    }

}
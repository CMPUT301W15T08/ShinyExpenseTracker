package ca.ualberta.cs.shinyexpensetracker.fragments;

import java.util.UUID;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import ca.ualberta.cs.shinyexpensetracker.R;
import ca.ualberta.cs.shinyexpensetracker.activities.AddDestinationActivity;
import ca.ualberta.cs.shinyexpensetracker.activities.utilities.IntentExtraIDs;
import ca.ualberta.cs.shinyexpensetracker.adapters.DestinationItemAdapter;
import ca.ualberta.cs.shinyexpensetracker.framework.Application;
import ca.ualberta.cs.shinyexpensetracker.framework.ExpenseClaimController;
import ca.ualberta.cs.shinyexpensetracker.framework.IView;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaim;

/**
 * Displays a list of destinations. Third of the tabs in the summary page.
 * 
 * Core Functionality generated by Eclipse when creating TabbedView
 */
public class DestinationListFragment extends Fragment implements IView<ExpenseClaim> {

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";
	private ExpenseClaim claim;
	private UUID claimID;
	private DestinationItemAdapter adapter;
	private AlertDialog lastDialog;

	private ExpenseClaimController controller;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static DestinationListFragment newInstance(int sectionNumber) {
		DestinationListFragment fragment = new DestinationListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.tab_destinations_list, container, false);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		controller = Application.getExpenseClaimController();

		// Get the claim index to display
		Intent intent = getActivity().getIntent();
		claimID = (UUID) intent.getSerializableExtra(IntentExtraIDs.CLAIM_ID);
		if (claimID == null) {
			throw new RuntimeException("Intent not passed: Got a null claim ID.");
		}

		// Get the claim context
		claim = controller.getExpenseClaimByID(claimID);

		// Inform the model that we're listening for updates.
		claim.addView(this);

		// Set up view visibility
		setPromptVisibility();

		// Set up the list view to display data
		ListView destinations = (ListView) getView().findViewById(R.id.destinationsListView);
		adapter = new DestinationItemAdapter(claim, getActivity().getBaseContext());
		destinations.setAdapter(adapter);

		// -- On Click : Edit -- //
		destinations.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				editDestinationAt(position);
			}
		});

		// -- On Long Click : Delete -- //
		destinations.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				askDeleteDestinationAt(position);
				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/**
	 * Prompts the user for deletion of an destination at a given position
	 * 
	 * @param position
	 *            the position in the list view to delete
	 */
	public void askDeleteDestinationAt(final int position) {
		// Construct a new dialog to be displayed.
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// -- Delete this destination?
		builder.setTitle(getString(R.string.deleteDestinationItemPromptTitle));
		builder.setMessage(getString(R.string.deleteDestinationItemPromptMessage));
		// -- OK -- //
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// remove the Destination
				deleteDestinationAt(position);
				dialog.dismiss();
				// Nullify the last opened dialog so we can tell it was
				// dismissed
				lastDialog = null;
			}
		});
		// -- Cancel -- //
		builder.setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// do nothing
				dialog.dismiss();
				// Nullify the last opened dialog so we can tell it was
				// dismissed
				lastDialog = null;
			}
		});

		// Show the newly created dialog
		lastDialog = builder.create();
		lastDialog.show();
	}

	/**
	 * Opens the activity responsible for editing a claim
	 * 
	 * @param position
	 *            the position in the listview to edit.
	 */
	public void editDestinationAt(int position) {
		// Create an intent to edit an destination item
		Intent intent = new Intent(getActivity(), AddDestinationActivity.class);
		// --> Tell it that we're editing the index at this position
		intent.putExtra(IntentExtraIDs.CLAIM_ID, claimID);
		intent.putExtra(IntentExtraIDs.DESTINATION_ID, claim.getDestinationAtPosition(position).getID());

		// Start the activity with our edit intent
		startActivity(intent);
	}

	/**
	 * Deletes the destination at position without prompt
	 * 
	 * @param position
	 */
	public void deleteDestinationAt(int position) {
		claim.removeDestination(position);
	}

	@Override
	public void update(ExpenseClaim m) {
		// Model was updated, inform the views
		adapter.notifyDataSetChanged();
		setPromptVisibility();
	}

	/**
	 * Sets visibility for the prompt informing the user that there are no
	 * destinations. Should be called any time the list needs refreshing.
	 */
	private void setPromptVisibility() {
		// Stop if we're not bound to a view.
		if (getView() == null) {
			return;
		}

		TextView noDestinations = (TextView) getView().findViewById(R.id.noDestinationsTextView);

		// Are there destinations to display?
		if (claim.getDestinationCount() == 0) {
			// No.
			// Set the prompt to be visible
			noDestinations.setVisibility(View.VISIBLE);
		} else {
			// Yes.
			// Set the list to be visible
			noDestinations.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * The last displayed dialog of the fragment
	 * 
	 * @return Returns the last displayed dialog that is open, or null if it was
	 *         closed
	 */
	public AlertDialog getLastDialog() {
		return lastDialog;
	}
}

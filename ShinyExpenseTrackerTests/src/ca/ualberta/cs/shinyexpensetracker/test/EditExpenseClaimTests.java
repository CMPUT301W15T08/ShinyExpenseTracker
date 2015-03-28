package ca.ualberta.cs.shinyexpensetracker.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.ualberta.cs.shinyexpensetracker.activities.ExpenseClaimActivity;
import ca.ualberta.cs.shinyexpensetracker.framework.Application;
import ca.ualberta.cs.shinyexpensetracker.framework.ExpenseClaimController;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaim;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaimList;
import ca.ualberta.cs.shinyexpensetracker.test.mocks.MockExpenseClaimListPersister;
import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import ca.ualberta.cs.shinyexpensetracker.R;

/**
 * Tests various parts of the functionality of ExpenseClaimActivity that relates to 
 * editing existing ExpenseClaims.
 **/
@SuppressLint("SimpleDateFormat")
public class EditExpenseClaimTests extends ActivityInstrumentationTestCase2<ExpenseClaimActivity> {
	static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

	ExpenseClaimController	controller;
	Instrumentation			instrumentation;
	ExpenseClaimActivity	activity;

	EditText				startDateField, endDateField, nameField;
	Button					doneButton;
	
	ExpenseClaimList		list;
	ExpenseClaim 			claim;

	public EditExpenseClaimTests(Class<ExpenseClaimActivity> activityClass) {
		super(activityClass);
	}

	public EditExpenseClaimTests() {
		super(ExpenseClaimActivity.class);
	}

	/**
	 * Loads the activity with a test existing ExpenseClaim set.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		list = new ExpenseClaimList();
		claim = getStartingClaim();
		
		list.addClaim(claim);

		controller = new ExpenseClaimController(new MockExpenseClaimListPersister(list));
		Application.setExpenseClaimController(controller);
		
		Intent i = new Intent();
		i.putExtra(ExpenseClaimActivity.CLAIM_INDEX, 0);
		setActivityIntent(i);
		
		instrumentation = getInstrumentation();
		activity = (ExpenseClaimActivity) getActivity();
		
		nameField = (EditText) activity.findViewById(R.id.editTextExpenseClaimName);
		startDateField = (EditText) activity.findViewById(R.id.editTextStartDate);
		endDateField = (EditText) activity.findViewById(R.id.editTextEndDate);
		doneButton = (Button) activity.findViewById(R.id.addExpenseClaimDoneButton);
	}
	
	public void testThatFieldsWerePopulatedProperly() throws ParseException {
		assertEquals(claim.getName(), nameField.getText().toString());
		assertEquals(claim.getStartDate(), getDate(startDateField));
		assertEquals(claim.getEndDate(), getDate(endDateField));
	}

	private ExpenseClaim getStartingClaim() throws ParseException {
		Date startDate = sdf.parse("2015-01-01");
		Date endDate = sdf.parse("2015-01-02");

		return new ExpenseClaim("test", startDate, endDate,
				ExpenseClaim.Status.IN_PROGRESS);
	}
	
	private Date getDate(EditText dateField) throws ParseException {
		return sdf.parse(dateField.getText().toString());
	}
}

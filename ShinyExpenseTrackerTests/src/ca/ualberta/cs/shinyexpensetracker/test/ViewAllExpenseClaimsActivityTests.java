/**
 *  This Unit Test module tests the ExpenseClaimView activity,
 *  ensuring it is displayed correctly.
 *  
 *  Copyright (C) 2015  github.com/RostarSynergistics
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Issue #22
 * @author Tristan Meleshko
 */

package ca.ualberta.cs.shinyexpensetracker.test;

import java.util.Date;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import ca.ualberta.cs.shinyexpensetracker.ExpenseClaimController;
import ca.ualberta.cs.shinyexpensetracker.activities.ExpenseClaimsView;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaim;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaimList;
import ca.ualberta.cs.shinyexpensetracker.models.Tag;

public class ViewAllExpenseClaimsActivityTests extends
		ActivityInstrumentationTestCase2<ExpenseClaimsView> {
	
	ExpenseClaimsView activity;
	ExpenseClaimList claimsList;
	
	ListView claimListView;
	
	public ViewAllExpenseClaimsActivityTests() {
		super(ExpenseClaimsView.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// Inject an empty list so that saving/loading doesn't interfere,
		// just in case.
		claimsList = new ExpenseClaimList();
		ExpenseClaimController.getInstance().setClaimList(claimsList);
		
		activity = getActivity();
		claimListView = (ListView) activity.findViewById(ca.ualberta.cs.shinyexpensetracker.R.id.expense_claim_list);
	}
	
	/**
	 * Add a claim to the claimsList safely
	 * @param claim
	 * @return
	 */
	private ExpenseClaim addClaim(final ExpenseClaim claim) {
		// Run on the activity thread.
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override
			public void run() {
				claimsList.addClaim(claim);
			}
		});
		getInstrumentation().waitForIdleSync();
		return claim;
	}
	
	/**
	 * Delete a claim from the claimsList safely
	 * @param claim
	 */
	private void deleteClaim(final ExpenseClaim claim) {
		// Run on the activity thread
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override
			public void run() {
				claimsList.removeClaim(claim);
			}
		});
		getInstrumentation().waitForIdleSync();
	}

	private ExpenseClaim getClaim(int i) {
		return (ExpenseClaim) claimListView.getItemAtPosition(i);
	}
	
	/**
	 * Tests if the longPressDialog displays correctly.
	 * 
	 * ListViews don't seem to expose a performItemLongClick
	 * function. I can't test UI functionality without this, so
	 * we'll assume that long click works and calls an
	 * "askDeleteItemAtPosition" function correctly. That way,
	 * the unit test only need to test if this function does
	 * its job, rather than checking the API functionality itself.
	 * 
	 * --This test may be removed in future iterations.
	 */
	public void testLongPressDialog() {
		// Not this test's responsibility to check what was deleted.
		addClaim(new ExpenseClaim("Test Claim"));

		// Fake long press
		//
		// NOTE: This will probably give a "Window Leaked" warning.
		// This is because we're kidnapping the dialog from the activity
		// via return call after the activity closes. Methods that call
		// askDeleteClaimAt should NEVER keep the value.
		// In fact, the only reason it's exposed is because ListViews
		// don't seem to expose performItemLongClick.
		assertTrue(activity.askDeleteClaimAt(0).isShowing());
	}
	
	/**
	 * Adds a claim and ensures that it is visible in the listview.
	 */
	public void testAddedClaimIsVisible() {
		ExpenseClaim claim = addClaim(new ExpenseClaim("Test Claim"));
		ExpenseClaim visibleClaim;
		
		// Get the last position in the list
		// TODO Get last position. See issue #64.
		int lastPosition = claimListView.getCount() - 1;
		assertFalse("Claim List is empty", claimListView.getCount() == 0);
		assertFalse("Claim List has too many objects", claimListView.getCount() > 1);
		
		// Get the expense claim object
		visibleClaim = getClaim(lastPosition);
		assertNotNull("No object found in the list", visibleClaim);
		
		// Ensure that the claim that was added to the list
		// is also the claim in the listview.
		assertTrue("Claim not visible", visibleClaim.equals(claim));
	}
	
	/* All of the testClaimsSorted check roughly the same thing,
	 * going through all 6 permutations of {new, mid, old}.
	 */
	public void testClaimsSortedA() {
		ExpenseClaim newClaim = addClaim(new ExpenseClaim("New Claim", new Date(1000000)));
		ExpenseClaim midClaim = addClaim(new ExpenseClaim("Mid Claim", new Date(300000)));
		ExpenseClaim oldClaim = addClaim(new ExpenseClaim("Old Claim", new Date(10000)));

		assertEquals("new claim must come first in the list", newClaim, getClaim(0));
		assertEquals("mid claim must be in the middle of the list", midClaim, getClaim(1));
		assertEquals("old claim must come last in the list", oldClaim, getClaim(2));
	}
	public void testClaimsSortedB() {
		ExpenseClaim newClaim = addClaim(new ExpenseClaim("New Claim", new Date(1000000)));
		ExpenseClaim oldClaim = addClaim(new ExpenseClaim("Old Claim", new Date(10000)));
		ExpenseClaim midClaim = addClaim(new ExpenseClaim("Mid Claim", new Date(300000)));

		assertEquals("new claim must come first in the list", newClaim, getClaim(0));
		assertEquals("mid claim must be in the middle of the list", midClaim, getClaim(1));
		assertEquals("old claim must come last in the list", oldClaim, getClaim(2));
	}
	public void testClaimsSortedC() {
		ExpenseClaim midClaim = addClaim(new ExpenseClaim("Mid Claim", new Date(300000)));
		ExpenseClaim newClaim = addClaim(new ExpenseClaim("New Claim", new Date(1000000)));
		ExpenseClaim oldClaim = addClaim(new ExpenseClaim("Old Claim", new Date(10000)));

		assertEquals("new claim must come first in the list", newClaim, getClaim(0));
		assertEquals("mid claim must be in the middle of the list", midClaim, getClaim(1));
		assertEquals("old claim must come last in the list", oldClaim, getClaim(2));
	}
	public void testClaimsSortedD() {
		ExpenseClaim oldClaim = addClaim(new ExpenseClaim("Old Claim", new Date(10000)));
		ExpenseClaim newClaim = addClaim(new ExpenseClaim("New Claim", new Date(1000000)));
		ExpenseClaim midClaim = addClaim(new ExpenseClaim("Mid Claim", new Date(300000)));

		assertEquals("new claim must come first in the list", newClaim, getClaim(0));
		assertEquals("mid claim must be in the middle of the list", midClaim, getClaim(1));
		assertEquals("old claim must come last in the list", oldClaim, getClaim(2));
	}
	public void testClaimsSortedE() {
		ExpenseClaim midClaim = addClaim(new ExpenseClaim("Mid Claim", new Date(300000)));
		ExpenseClaim oldClaim = addClaim(new ExpenseClaim("Old Claim", new Date(10000)));
		ExpenseClaim newClaim = addClaim(new ExpenseClaim("New Claim", new Date(1000000)));

		assertEquals("new claim must come first in the list", newClaim, getClaim(0));
		assertEquals("mid claim must be in the middle of the list", midClaim, getClaim(1));
		assertEquals("old claim must come last in the list", oldClaim, getClaim(2));
	}
	public void testClaimsSortedF() {
		ExpenseClaim oldClaim = addClaim(new ExpenseClaim("Old Claim", new Date(10000)));
		ExpenseClaim midClaim = addClaim(new ExpenseClaim("Mid Claim", new Date(300000)));
		ExpenseClaim newClaim = addClaim(new ExpenseClaim("New Claim", new Date(1000000)));

		assertEquals("new claim must come first in the list", newClaim, getClaim(0));
		assertEquals("mid claim must be in the middle of the list", midClaim, getClaim(1));
		assertEquals("old claim must come last in the list", oldClaim, getClaim(2));
	}
	
	/**
	 * Deletes a claim and ensure it isn't visible in the listview.
	 * Does not test dialogs.
	 */
	public void testDeleteVisibleClaim() {
		// Build 2 claims with dates so that claim 1 < claim 2.
		ExpenseClaim claim1 = addClaim(
				new ExpenseClaim("Delete Claim 1", new Date(), null, ExpenseClaim.Status.IN_PROGRESS, new Tag("Test 1"))
			);
		ExpenseClaim claim2 = addClaim(
				new ExpenseClaim("Delete Claim 2", new Date(), null, ExpenseClaim.Status.IN_PROGRESS, new Tag("Test 2"))
			);
		ExpenseClaim visibleClaim;
		
		// Check that an item is actually deleted
		deleteClaim(claim2);
		assertEquals("Item wasn't deleted", 1, claimListView.getCount());
		
		// Check that the right item was deleted.
		visibleClaim = getClaim(0);
		assertEquals("Remaining claim isn't claim 1", claim1, visibleClaim);

		// Check that the remaining item is actually deleted
		deleteClaim(visibleClaim);
		assertEquals("Item wasn't deleted", 0, claimListView.getCount());
	}

	/**
	 * Checks if pressing the NewClaim button opens the
	 * appropriate activity.
	 */
	public void testMenuNewClaim() {
		
		// Execute the New Claim button from the Options menu synchronously.
		assertTrue(getInstrumentation().invokeMenuActionSync(
				activity, ca.ualberta.cs.shinyexpensetracker.R.id.action_new_claim,
				0));
		// Wait for the application to become idle
		getInstrumentation().waitForIdleSync();

		// TODO #17 Check if the activity was opened.
		// Here, we're checking if the "New Claim" button adds a new faux claim
		assertEquals("Claim not added", 1, claimListView.getCount());
		
		// Fail this test because it isn't finished.
		fail();
	}
}
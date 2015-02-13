package ca.ualberta.cs.shinyexpensetracker.test;

import java.util.Calendar;
import java.util.Date;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ArrayAdapter;

public class ViewSubmittedExpenseClaimDetailActivityTests extends
		ActivityInstrumentationTestCase2<ViewSubmittedExpenseClaimDetailActivity> {

	private Instrumentation instrumentation;

	protected void setUp() throws Exception {
		super.setUp();
		
		instrumentation = getInstrumentation();
	}

	public void testThatViewDetailsWork() {
		int id = 1;
		String name = "Jane Smith";
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Date startDate = cal.getTime();
		cal.add(Calendar.DATE, 1);
		Date endDate = cal.getTime();
		
		ExpenseClaim testClaim = new ExpenseClaim();
		testClaim.setId(id);
		testClaim.setName(name);
		testClaim.setStartDate(startDate);
		testClaim.setEndDate(endDate);
		
        ViewSubmittedExpenseClaimDetailActivity activity = startWithIntent(testClaim);
        
        assertEquals("Names not the same", name, activity.getName());
        assertEquals("Start Dates not the same", startDate, activity.getStartDate());
        assertEquals("End Dates not the same", endDate, activity.getEndDate());
	}
	
	public void testThatAddingACommentWorks() {
		int id = 1;
		String name = "Jane Smith";
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Date startDate = cal.getTime();
		cal.add(Calendar.DATE, 1);
		Date endDate = cal.getTime();
		
		ExpenseClaim testClaim = new ExpenseClaim();
		testClaim.setId(id);
		testClaim.setName(name);
		testClaim.setStartDate(startDate);
		testClaim.setEndDate(endDate);
		
        final ViewSubmittedExpenseClaimDetailActivity activity = startWithIntent(testClaim);
        final String commentText = "New comment";
        
        instrumentation.runOnMainSync(new Runnable() {
			
			@Override
			public void run() {
				activity.getCommentsButton().performClick();
				activity.setCommentText(commentText);
				activity.getAddCommentButton().performClick();
			}
		});
        
        ArrayAdapter<Comment> adapter = activity.getCommentsAdapter();
        assertEquals("Comments adapter has less or more items than expected", 1, adapter.getCount());
        assertEquals("Comment doesn't have correct text", commentText, adapter[0].getText());
	}
	
	private ViewSubmittedExpenseClaimDetailActivity startWithIntent(ExpenseClaim claim) {
		Intent intent = new Intent();
		intent.putExtra(ViewSubmittedExpenseClaimDetailActivity.CLAIM_ID_KEY, claim.id);
		setActivityIntent(intent);
		return (ViewSubmittedExpenseClaimDetailActivity) getActivity();
	}
}

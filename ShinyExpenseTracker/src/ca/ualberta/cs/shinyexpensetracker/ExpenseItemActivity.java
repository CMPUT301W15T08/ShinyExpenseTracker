 /** 
 * Covers Issue 5
 * Things to implement: saving of an expenseItem to an expenseClaim
 * @author Sarah Morris
 * @version 1.0
 * @since 2015-03-09
 *
 * 
 * Displays activity_create_expense_item activity, to give the user an 
 * interface to add the name, date, category, amount spent, currency, 
 * description and a photo of a receipt for expense items. 
 */

package ca.ualberta.cs.shinyexpensetracker;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaim;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseItem;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseItem.Category;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseItem.Currency;

public class ExpenseItemActivity extends Activity implements OnClickListener{

	// DatePickerDialog from: 
	//  	http://androidopentutorials.com/android-datepickerdialog-on-edittext-click-event/
	// On March 2 2015	  
    private EditText date;
    private DatePickerDialog datePickerDialog;    
    private SimpleDateFormat dateFormatter;
    private ImageButton button;
    private Uri imageFileUri;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private AlertDialog.Builder adb;
    public Dialog alertDialog;
    private boolean isEditing = false;
    private ExpenseItem item;
    private ExpenseClaim claim;
    private HashMap<String, Integer> categoriesMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> currenciesMap = new HashMap<String, Integer>();
    private ExpenseClaimController controller;
    
    public Resources getLocalResources(){
    	return getResources();
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_expense_item);
		
		dateFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.CANADA);

        
        button = (ImageButton) findViewById(R.id.expenseItemReceiptImageButton);
        
        Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		
		if (bundle != null){
			int claimId = (Integer) bundle.get("Claim ID");
			Integer expenseItemId = (Integer) bundle.get("Item ID");
			controller = ExpenseClaimController.getInstance();
			claim = controller.getExpenseClaim(claimId);
			if (expenseItemId != null){
				item = claim.getItemById(expenseItemId);
				isEditing = true;
				populateTextViews();
			}
		}
		adb = new AlertDialog.Builder(this);
        
        findViewsById();
        
        setDateTimeField();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.expense_item, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setEditTextValue(int textViewId, String value){
		EditText tv = (EditText) findViewById(textViewId);
		tv.setText(value);
	}
	
	private void populateTextViews(){
		populateHashMaps();
		
		//SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.CANADA);
		
		setEditTextValue(R.id.expenseItemNameEditText, item.getName().toString());
		setEditTextValue(R.id.expenseItemDateEditText, dateFormatter.format(item.getDate()));
		setEditTextValue(R.id.expesenItemDescriptionEditText, item.getDescription().toString());
		setEditTextValue(R.id.expenseItemAmountEditText, item.getAmountSpent().toString());
		Spinner s = (Spinner) findViewById(R.id.expenseItemCategorySpinner);
		s.setSelection(categoriesMap.get(item.getCategory().getText()));
		s = (Spinner) findViewById(R.id.expenseItemCurrencySpinner);
		s.setSelection(currenciesMap.get(item.getCurrency().name()));
		
		if (item.doesHavePhoto()){
			button.setDrawingCacheEnabled(true);
			button.setImageBitmap(item.getReceiptPhoto());
		}
	}
	
	// copied from https://stackoverflow.com/questions/26842530/roundedimageview-add-border-and-shadow
	// on March 15, 2015
	public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }
	
	private void populateHashMaps() {
		categoriesMap.put("air fare", 0);
		categoriesMap.put("ground transport", 1);
		categoriesMap.put("vehicle rental", 2);
		categoriesMap.put("private automobile", 3);
		categoriesMap.put("fuel", 4);
		categoriesMap.put("parking", 5);
		categoriesMap.put("registration", 6);
		categoriesMap.put("accomodation", 7);
		categoriesMap.put("meal", 8);
		categoriesMap.put("supplies", 9);

		currenciesMap.put("CAD", 0);
		currenciesMap.put("USD", 1);
		currenciesMap.put("GBP", 2);
		currenciesMap.put("EUR", 3);
		currenciesMap.put("CHF", 4);
		currenciesMap.put("JPY", 5);
		currenciesMap.put("CNY", 6);
		
	}

	private void findViewsById() {
    	date = (EditText) findViewById(R.id.expenseItemDateEditText);
        date.setInputType(InputType.TYPE_NULL);
        date.requestFocus();
    }
 
	/**
	 * Uses a DatePickerDialog to allow user to choose a date
	 */
    private void setDateTimeField() {
        date.setOnClickListener(this);
        date.setOnClickListener(this);
        
        Calendar newCalendar = Calendar.getInstance();
        
        datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {
 
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                date.setText(dateFormatter.format(newDate.getTime()));
            }
 
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
     
    }
	
    /** 
     * displays a datePickerDialog when expenseItemDateTextView is clicked.
     */
    @Override
    public void onClick(View view) {
        if(view == date) {
            datePickerDialog.show();
        }
    }

    /** 
     * Gets inputed data from text fields and spinners, saving them in an expenseItem object.
     * @param v
     * @throws ParseException
     */
	public boolean createExpenseItem(View v) throws ParseException {
		
		EditText nameText = (EditText) findViewById(R.id.expenseItemNameEditText);
		EditText dateText = (EditText) findViewById(R.id.expenseItemDateEditText);
		Spinner categorySpinner = (Spinner) findViewById(R.id.expenseItemCategorySpinner);
		EditText amountText = (EditText) findViewById(R.id.expenseItemAmountEditText);
		Spinner currencySpinner = (Spinner) findViewById(R.id.expenseItemCurrencySpinner);
		EditText descriptionText = (EditText) findViewById(R.id.expesenItemDescriptionEditText);
		
		//get the name of the expense item
		String name = "";
		if (nameText.getText().length() == 0){
			//display dialog if no name entered
			adb.setMessage("Expense Item requires a name");
			adb.setCancelable(true);
			adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) { }
			});
			alertDialog = adb.create();
			alertDialog.show();
			return false;
		} else {
			name = nameText.getText().toString();
		}
		
		// get the date of the expense item
		Date date = new Date();
		if (dateText.getText().length() == 0) {
			//display dialog if no date entered
			adb.setMessage("Expense Item requires a date");
			adb.setCancelable(true);
			adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) { }
			});
			alertDialog = adb.create();
			alertDialog.show();
			return false;
		} else {
			date = dateFormatter.parse(dateText.getText().toString());
		}
		
		//get the current selection of the category spinner
		Category category = Category.fromString(categorySpinner.getSelectedItem().toString());
		
		// get the amount Spent from the editText, checking if not entered
		BigDecimal amount = new BigDecimal("0.00");
		if (amountText.getText().length() == 0) {
			//display error dialog if no amount spent has been entered
			adb.setMessage("Expense Item requires an amount spent");
			adb.setCancelable(true);
			adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) { }
			});
			alertDialog = adb.create();
			alertDialog.show();
			return false;
		} else {
			amount = new BigDecimal(amountText.getText().toString());
		}
		
		//get the current selection of the currencySpinner
		Currency currency = Currency.valueOf(currencySpinner.getSelectedItem().toString());
		
		//get the description entered 
		String description;
		if (descriptionText.getText().length() == 0){
			description = "";
		} else {
			description = descriptionText.getText().toString();
		}
		
		ExpenseItem expense = new ExpenseItem(name, date, category, amount, 
				currency, description, button.getDrawingCache()); 
		
		if (isEditing){
			claim.removeItem(item);
			claim.addItem(expense);
		}
		else{
			claim.addItem(expense);
		}
		//Still needs to be implemented
		//Add expenseItem to claim
		
		return true;
	}

	
	/** 
	 * Runs on click of the expenseItemImageButton.  Opens Camera app to allow user to take a 
	 * picture of a receipt.  Saves picture in a temporary file.
	 * @param V
	 */
	//Source: https://github.com/saemorris/BogoPicLab/tree/master/CameraTest
	public void takePicture(View V) {
		// Create a folder to store pictures
		String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
		File folderF = new File(folder);
		if (!folderF.exists()) {
			folderF.mkdir();
		}
			
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Create an URI for the picture file
		String imageFilePath = folder + "/" + String.valueOf(System.currentTimeMillis()) + ".jpg";
		File imageFile = new File(imageFilePath);
		imageFileUri = Uri.fromFile(imageFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
				
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	/**
	 * Runs on return from the camera app.  Displays the image taken in the 
	 * expenseItemImageButton
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Result: OK!", Toast.LENGTH_SHORT).show();
				assert imageFileUri != null;
				assert button != null;
				button.setImageDrawable(Drawable.createFromPath(imageFileUri.getPath()));
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Result: Cancelled", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Result: ???", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/** On click of the expenseItemDoneButton, the activity will close to 
	 * return to the previous activity (ExpenseItems Summary page)
	 * @param v
	 * @throws ParseException 
	 */
	public void doneExpenseItem(View v) throws ParseException{
		
		if (createExpenseItem(v)){
			finish();
		}
	}
	
	/** 
	 * Method to allow for testing of the DatePickerDialog
	 * @return
	 */
	public DatePickerDialog getDialog(){
		return datePickerDialog;
	}
}

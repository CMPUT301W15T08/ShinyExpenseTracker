package ca.ualberta.cs.shinyexpensetracker.activities;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ca.ualberta.cs.shinyexpensetracker.R;
import ca.ualberta.cs.shinyexpensetracker.framework.Application;
import ca.ualberta.cs.shinyexpensetracker.framework.IView;
import ca.ualberta.cs.shinyexpensetracker.framework.TagController;
import ca.ualberta.cs.shinyexpensetracker.models.Tag;
import ca.ualberta.cs.shinyexpensetracker.models.TagList;
import ca.ualberta.cs.shinyexpensetracker.utilities.InAppHelpDialog;

/**
 * Activity that handles viewing all the tags.  Enables user to add, edit and delete tags 
 * Covers Issue #24, 74
 * 
 * @version 1.0
 * @since 2015-03-10
 */
public class ManageTagActivity extends Activity implements IView<TagList> {

	private ListView manageTags;
	private ArrayAdapter<Tag> tagListAdapter;
	private TagController tagController;
	private Button done;
	protected static AlertDialog alertDialogAddTag;
	protected static AlertDialog alertDialogEditTag;
	protected static AlertDialog alertDialogDeleteTag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_tag);
		manageTags = (ListView) findViewById(R.id.listViewManageTags);
		tagController = Application.getTagController();
		done = (Button) findViewById(R.id.doneButtonManageTags);
		
		// Setting a listener for tag controller
		tagController.getTagList().addView(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_tag, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Setting the list view
		tagListAdapter = new ArrayAdapter<Tag>(this,
				android.R.layout.simple_list_item_1, tagController.getTagList()
						.getTags());
		manageTags.setAdapter(tagListAdapter);

		manageTags.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				editTagFromDialog(position);
			}

		});

		manageTags.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				deleteTagFromDialog(position);
				return true;
			}

		});
		
		done.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addTagFromDialogue();
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_help) {
			InAppHelpDialog.showHelp(this, R.string.help_manage_tags);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Extracted method from onOptionsItemSelected that shows a dialog to add a
	 * tag if the menu button is pressed
	 * 
	 * @return true to indicate the function of the menu button has been
	 *         achieved
	 */
	@SuppressLint("InflateParams")
	private boolean addTagFromDialogue() {
		// Creating the dialog from a custom layout and getting all the widgets
		// needed
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater layoutInflater = this.getLayoutInflater();
		View dialogView = layoutInflater.inflate(R.layout.dialog_tag_input, null);
		builder.setView(dialogView);
		final EditText tagNameTextBox = (EditText) dialogView
				.findViewById(R.id.EditTextDialogTag);

		// Set the correct text
		TextView dialogTextView = (TextView) dialogView
				.findViewById(R.id.TextViewDialogInputType);
		dialogTextView.setText("Name for the new Tag:");

		// Setting the positive button to save the text in the dialog as a tag
		// if valid
		builder.setPositiveButton("Add Tag",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String tagName = tagNameTextBox.getText().toString();
						Tag tag = new Tag(tagName);
						if (tagController.inTagList(tag)) {
							Toast.makeText(getApplicationContext(),
									"Tag already exists", Toast.LENGTH_LONG)
									.show();
							return;
						}

						try {
							if (!tagController.addTag(tag)) {
								Toast.makeText(
										getApplicationContext(),
										"Tag name has to be an alphanumeric value.",
										Toast.LENGTH_LONG).show();
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});

		// Setting the negative button to close the dialog
		builder.setNegativeButton("Cancel",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		alertDialogAddTag = builder.create();
		alertDialogAddTag.show();

		return true;
	}

	/**
	 * Extracted method from the listview onClickListener dialog to edit a tag
	 * if the tag is pressed from the listview
	 * 
	 * @param pos
	 *            of the list view clicked
	 */
	@SuppressLint("InflateParams")
	private void editTagFromDialog(final int pos) {
		// Creating the dialog from a custom layout and getting all the widgets
		// needed
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater layoutInflater = this.getLayoutInflater();
		View dialogView = layoutInflater.inflate(R.layout.dialog_tag_input, null);
		builder.setView(dialogView);

		// Set the correct text
		TextView dialogTextView = (TextView) dialogView
				.findViewById(R.id.TextViewDialogInputType);
		dialogTextView.setText("Edit tag name:");

		// Displaying the old tag name to edit
		final EditText tagNameTextBox = (EditText) dialogView
				.findViewById(R.id.EditTextDialogTag);
		tagNameTextBox.setText(tagController.getTag(pos).getValue());

		// Setting the buttons
		builder.setPositiveButton("Edit Tag",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String tagName = tagNameTextBox.getText().toString();
						Tag tag = new Tag(tagName);
						if (tagController.inTagList(tag)) {
							Toast.makeText(getApplicationContext(),
									"Tag already exists", Toast.LENGTH_LONG)
									.show();
							return;
						}

						try {
							if (!tagController.editTag(pos, tag)) {
								Toast.makeText(
										getApplicationContext(),
										"Tag name has to be an alphanumeric value.",
										Toast.LENGTH_LONG).show();
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});

		builder.setNegativeButton("Cancel",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		alertDialogEditTag = builder.create();
		alertDialogEditTag.show();

	}

	/**
	 * Extracted method from the listview onLongItemClickListener dialog to
	 * delete a tag if the tag is long pressed from the listview
	 * 
	 * @param position
	 *            position in the list view that was clicked
	 */
	@SuppressLint("InflateParams")
	public void deleteTagFromDialog(final int position) {
		// Creating a delete dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater layoutInflater = this.getLayoutInflater();
		View dialogView = layoutInflater.inflate(R.layout.dialog_delete_tag, null);
		builder.setView(dialogView);

		// Setting buttons
		builder.setPositiveButton("Delete Tag",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Tag tag = (Tag) manageTags.getItemAtPosition(position);
							if (!tagController.deleteTag(tag)) {
								Toast.makeText(getApplicationContext(),
										"Tag could not be deleted",
										Toast.LENGTH_LONG).show();
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});

		builder.setNegativeButton("Cancel",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		alertDialogDeleteTag = builder.create();
		alertDialogDeleteTag.show();

	}

	@Override
	public void update(TagList m) {
		tagListAdapter.notifyDataSetChanged();
	}

	public static AlertDialog getDialog() {
		return alertDialogAddTag;
	}

	public static AlertDialog getEditDialog() {
		return alertDialogEditTag;
	}

	public static AlertDialog getDeleteDialog() {
		return alertDialogDeleteTag;
	}
}

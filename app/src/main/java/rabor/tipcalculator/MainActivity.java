package rabor.tipcalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.View.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.view.View.OnKeyListener;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity
implements OnEditorActionListener, OnKeyListener {

    private static final String TAG = "MainActivity";

    // define member variables for the widgets
    private TextView percentTV;
    private TextView tipPercentTV;
    private TextView totalTV;
    private EditText billET;
    private Button percentUpButton;
    private Button percentDownButton;
    private Button mResetButton;
    private CheckBox mCheckBox;
    private RadioGroup mRadioGroup;
    private RadioButton mNoneRadioButton;
    private RadioButton mTipRadioButton;
    private RadioButton mTotalRadioButton;
    private Spinner mSplitSpinner;
    private TextView mPerPersonLabel;
    private TextView mPerPersonTV;


    // define rounding constants
    private final int ROUND_NONE = 0;
    private final int ROUND_TIP = 1;
    private final int ROUND_TOTAL = 2;

    // define instance variable
    private String billAnountString = "";
    private float tipPercent = .15f;
    private SharedPreferences mSavedValues;
    private int mRounding = ROUND_NONE;
    private int mSplit = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get reference to the widget
        percentTV = (TextView) findViewById(R.id.percentTV);
        tipPercentTV = (TextView) findViewById(R.id.tipPercentTV);
        totalTV = (TextView) findViewById(R.id.totalTV);
        billET = (EditText) findViewById(R.id.billET);
        percentUpButton = (Button) findViewById(R.id.percentUpButton);
        percentDownButton = (Button) findViewById(R.id.percentDownButton);
        mResetButton = (Button) findViewById(R.id.resetBotton);
        mCheckBox = (CheckBox) findViewById(R.id.checkBox);
        mRadioGroup = (RadioGroup) findViewById(R.id.roundingRadioGroup);
        mNoneRadioButton = (RadioButton) findViewById(R.id.noneRadioButton);
        mTipRadioButton = (RadioButton) findViewById(R.id.tipRadioButton);
        mTotalRadioButton = (RadioButton) findViewById(R.id.totalRadioButton);
        mSplitSpinner = (Spinner) findViewById(R.id.splitSpinner);
        mPerPersonLabel = (TextView) findViewById(R.id.perPersonLabel);
        mPerPersonTV = (TextView) findViewById(R.id.perPersonTV);

        // set array adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.split_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSplitSpinner.setAdapter(adapter);

        // set Listeners
        // current class as the listener
        billET.setOnEditorActionListener(this);
        billET.setOnKeyListener(this);
        mRadioGroup.setOnKeyListener(this);

        // named class as the listener
        ClickListener clickListener = new ClickListener();
        percentUpButton.setOnClickListener(clickListener);
        percentDownButton.setOnClickListener(clickListener);
        mResetButton.setOnClickListener(clickListener);
        mCheckBox.setOnClickListener(clickListener);

        // anonymous class as the listener
        mRadioGroup.setOnCheckedChangeListener(checkedChangeListener);

        // aonymous inner class as the listener
        mSplitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSplit = position + 1;
                calculateAndDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        // get the Shared Preferences
        mSavedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (keyCode) {

            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                calculateAndDisplay();
                imm.hideSoftInputFromInputMethod(
                        billET.getWindowToken(), 0);
                return true; // consume the event
            case KeyEvent.KEYCODE_DPAD_DOWN:
                calculateAndDisplay();
                imm.hideSoftInputFromInputMethod(
                        billET.getWindowToken(), 0);
                break; // don't consume the even
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(v.getId() == R.id.percentTV) {
                    calculateAndDisplay();
                }

                break; // don't consume the event
        }

        return false; // don't consume the event
    }


    class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {

            switch(v.getId()) {
                case R.id.percentUpButton:
                    tipPercent = tipPercent + .01f;
                    calculateAndDisplay();
                    break;
                case R.id.percentDownButton:
                    tipPercent = tipPercent - .01f;
                    calculateAndDisplay();
                    break;
                case R.id.resetBotton:
                    tipPercent = .15f;
                    billET.setText("");
                    calculateAndDisplay();
                    break;
                case R.id.checkBox:
                    if(((CheckBox)v).isChecked()) {
                        billET.setText("");
                    }
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if(actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }

        Toast.makeText(getApplicationContext(), "Action ID: " + actionId, Toast.LENGTH_LONG).show();

        return false;
    }

    private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            switch (checkedId) {
                case R.id.noneRadioButton:
                    mRounding = ROUND_NONE;
                    break;
                case R.id.tipRadioButton:
                    mRounding = ROUND_TIP;
                    break;
                case R.id.totalRadioButton:
                    mRounding = ROUND_TOTAL;
                    break;
            }

            calculateAndDisplay();
        }
    };

    private void calculateAndDisplay() {

        // get the bill amount
        billAnountString = billET.getText().toString();
        float billAmount;
        if (billAnountString.equals("")) {
            billAmount = 0;
        } else {
            billAmount = Float.parseFloat(billAnountString);
        }

        Log.d(TAG, "Bill Amount: " + billAmount);

        // calcute tip and total
        float tipAmount = 0;
        float totalAmount = 0;
        if(mRounding == ROUND_NONE) {
            tipAmount = billAmount * tipPercent;
            totalAmount = tipAmount + billAmount;
        } else if(mRounding == ROUND_TIP) {
            tipAmount = StrictMath.round(billAmount * tipPercent);
            totalAmount = tipAmount + billAmount;
        } else if(mRounding == ROUND_TOTAL) {
            float tipNotRounded = billAmount * tipPercent;
            totalAmount = StrictMath.round(billAmount + tipNotRounded);
            tipAmount = totalAmount - billAmount;
        }

        // calculate split amount and show / hide split amount widget
        float splitAmount = 0;
        if(mSplit == 1) {
            mPerPersonLabel.setVisibility(View.GONE);
            mPerPersonTV.setVisibility(View.GONE);
        } else {
            splitAmount = totalAmount / mSplit;
            mPerPersonLabel.setVisibility(View.VISIBLE);
            mPerPersonTV.setVisibility(View.VISIBLE);
        }

        // display the formatted results
        NumberFormat percent = NumberFormat.getPercentInstance();
        percentTV.setText(percent.format(tipPercent));

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipPercentTV.setText(currency.format(tipAmount));
        totalTV.setText(currency.format(totalAmount));
        mPerPersonTV.setText(currency.format(splitAmount));
    }

    @Override
    protected void onPause() {
        String key = String.valueOf(mCheckBox.getText());
        // save the instance variables
        Editor editor = mSavedValues.edit();
     //   if(mCheckBox.isChecked()) {
      //      editor.remove("billAmountString");
       // editor.clear()
        //editor.clear();
    //        mCheckBox.setChecked(false);
    //    } else {
            editor.putString("billAmountString", billAnountString);
            editor.putFloat("tipPercent", tipPercent);
     //   }
        editor.putInt("rounding", mRounding);
        editor.putInt("split", mSplit);

        editor.apply();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get the instance variables
        billAnountString = mSavedValues.getString("billAmountString", "");
        tipPercent = mSavedValues.getFloat("tipPercent", .15f);
        mRounding = mSavedValues.getInt("rounding", ROUND_NONE);
        mSplit = mSavedValues.getInt("split", 1);

        // set the bill amount on its widget
        billET.setText(billAnountString);

        if(mRounding == ROUND_NONE) {
            mNoneRadioButton.setChecked(true);
        } else if(mRounding == ROUND_TIP) {
            mTipRadioButton.setChecked(true);
        } else if(mRounding == ROUND_TOTAL) {
            mTotalRadioButton.setChecked(true);
        }

        // set split on spinner
        int position = mSplit - 1;
        mSplitSpinner.setSelection(position);

        // calculate and display
        //calculateAndDisplay();
    }

}

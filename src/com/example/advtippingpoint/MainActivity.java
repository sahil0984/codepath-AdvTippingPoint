package com.example.advtippingpoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int NUM_ROWS = 2;
	private static final int NUM_COLS = 8; //8
	private static final int MAX_TIP_PERCENT = 50;
	private static final int DEF_TIP_PERCENT = 15;
	private static final int DEF_TIP_STEPSIZE = 5;

	
	private TextView lblTotalAmount;
	private EditText etEditTotalAmount;
	
	private TextView tvTotalSplitWays;
	private ImageView ivButtons[][] = new ImageView[NUM_ROWS][NUM_COLS];
	
	private TextView tvTipSplitWays;
	private SeekBar sbTipSplitWays;
	
	private TextView tvTipPercent;
	private SeekBar sbTipPercent;
	
	private TextView tvTipAmount;
	
	private int totalSplitWays;
	private int tipSplitWays;
	private float totalAmount;
	private int tipPercent;
	private float totalTip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		lblTotalAmount = (TextView) findViewById(R.id.lblTotalAmount);
		etEditTotalAmount = (EditText) findViewById(R.id.etTotalAmount);
		
		tvTotalSplitWays = (TextView) findViewById(R.id.lblSplitBetween);
		
		tvTipSplitWays = (TextView) findViewById(R.id.lblSplitTipBetween);
		sbTipSplitWays = (SeekBar) findViewById(R.id.sbSplitTipBetween);
		
		tvTipPercent = (TextView) findViewById(R.id.lblTipPercent);
		sbTipPercent = (SeekBar) findViewById(R.id.sbTipPercent);
		
		tvTipAmount = (TextView) findViewById(R.id.lblTipAmount);
		
		
		Typeface font1 = Typeface.createFromAsset(getAssets(), "fonts/6216.ttf");
		lblTotalAmount.setTypeface(font1);
		tvTotalSplitWays.setTypeface(font1);
		tvTipSplitWays.setTypeface(font1);
		tvTipPercent.setTypeface(font1);
		tvTipAmount.setTypeface(font1);
		
		try 
		{
			totalAmount = Float.parseFloat(etEditTotalAmount.getText().toString());
		} 
		catch(NumberFormatException e) 
		{
			totalAmount = 0;
		}
		lblTotalAmount.setText("Total Amount: $" + totalAmount);

		
		etEditTotalAmount.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				lblTotalAmount.setText("Total Amount: $" + etEditTotalAmount.getText().toString());
				calcTip();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {				
			}
		});
		

		readParams();
		tvTotalSplitWays.setText("Split amount " + totalSplitWays +" ways");

		//totalSplitWays = 1;
		//tipPercent = DEF_TIP_PERCENT;
		

		tipSplitWays = totalSplitWays;
		tvTipSplitWays.setText("Split tip " + tipSplitWays +" ways");
		sbTipSplitWays.setMax(tipSplitWays-1);
		sbTipSplitWays.setProgress(tipSplitWays-1);
		sbTipSplitWays.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser == true) {

					tvTipSplitWays.setText("Split tip " + (progress+1) + " ways");
					tipSplitWays = progress+1;
					calcTip();
					preUpdateIvButtons();
				}
			}
		});
		
		
		tvTipPercent.setText("Tip Percentage: " + tipPercent + "%");
		sbTipPercent.setMax(MAX_TIP_PERCENT);
		sbTipPercent.setProgress(tipPercent);
		sbTipPercent.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				int stepSize = DEF_TIP_STEPSIZE;
				progress = ((int)Math.round(progress/stepSize))*stepSize;
			    seekBar.setProgress(progress);
			    
				tvTipPercent.setText("Tip Percentage: " + progress + "%");
				tipPercent = progress;
				writeParams();
				calcTip();
			}
		});
				
		populateSplitBetweenButtons();

		calcTip();
		
	}
	
	private void populateSplitBetweenButtons() {
		TableLayout table = (TableLayout) findViewById(R.id.tableSplitBetweenButtons);
		
		for (int row=0; row<NUM_ROWS; row++) {
			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new TableLayout.LayoutParams(
					TableLayout.LayoutParams.MATCH_PARENT,
					TableLayout.LayoutParams.MATCH_PARENT,
					1.0f));
			table.addView(tableRow);
			
			for (int col=0; col<NUM_COLS; col++) {
				ImageView ivButton = new ImageView(this);
				ivButton.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.MATCH_PARENT,
						1.0f));
				//ivButton.setScaleX((float) 0.5);
				//ivButton.setScaleY((float) 0.5);
				ivButton.setPadding(0, 0, 0, 0);
				ivButton.setImageResource(R.drawable.person_grey);
				
				//if (totalSplitWays > (row*NUM_COLS)+col) {
				//	ivButton.setImageResource(R.drawable.person_black);
				//} else {
				//	ivButton.setImageResource(R.drawable.person_grey);
				//}
				
				tableRow.addView(ivButton);
				ivButton.setId((row*NUM_COLS)+col);
				
				ivButtons[row][col] = ivButton;
				
				
				//preUpdateIvButtons();
				
				ivButton.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						TableLayout table = (TableLayout) findViewById(R.id.tableSplitBetweenButtons);
						ImageView ivButton = (ImageView) v;
						
						totalSplitWays = ivButton.getId() + 1;
						writeParams();
						tvTotalSplitWays.setText("Split amount " + totalSplitWays +" ways");
						
						tipSplitWays = totalSplitWays;//(tipSplitWays>totalSplitWays)?totalSplitWays:tipSplitWays;
						
						updateTipSplitWaysViews();
	
						calcTip();

						//for (int row=0; row<NUM_ROWS; row++) {							
						//	for (int col=0; col<NUM_COLS; col++) {
						//		
						//		if (ivButton.getId() >= (row*NUM_COLS)+col) {
						//			ivButtons[row][col].setImageResource(R.drawable.person_black);
						//		} else {
						//			ivButtons[row][col].setImageResource(R.drawable.person_grey);
						//		}
						//	}
						//}
						preUpdateIvButtons();
						
					
					}

				});
				
			}
		}
		
		preUpdateIvButtons();
		
	}

	public void calcTip() {
		float totalAmount;
		try
		{
		    totalAmount = Float.parseFloat(etEditTotalAmount.getText().toString());
			totalTip = (float) (totalAmount * tipPercent / 100);
		}
		catch (NumberFormatException e)
		{
			totalAmount = 0;
			totalTip = 0;
			//Toast.makeText(getApplicationContext(), "Enter an amount.",
			//		   Toast.LENGTH_SHORT).show();
		}
		float finalAmount = totalAmount + totalTip;
		float amountPerPerson = totalAmount/totalSplitWays;
		float tipPerPerson = totalTip/tipSplitWays;
		float totalPerPersonWithTip = amountPerPerson + tipPerPerson;
		float totalPerPersonNoTip = amountPerPerson;
		//tvTipAmount.setText((String) "Tip is: $" + String.format("%.2f",totalTip));
		tvTipAmount.setText( Html.fromHtml((String) (
									   "Total amount to be paid: $" + "<b>" + String.format("%.2f",finalAmount) + "</b>" + "<br>" 
									 + "Including total tip: $" + "<b>" + String.format("%.2f",totalTip) + "</b>" + "<br><br>"
									 + "Pay $" + "<b>" + String.format("%.2f",totalPerPersonWithTip) + "</b>" + " if you are paying the tip" + "<br>"
									 + "(including $" + "<b>" + String.format("%.2f",tipPerPerson) + "</b>" + " tip)" + "<br>"
									 + "Pay $" + "<b>" + String.format("%.2f",totalPerPersonNoTip) + "</b>" + " if you are not paying the tip" )
									      )
						   );
	}
	
	public void onIncSplitWays(View v) {
		if (totalSplitWays>47) {
			Toast.makeText(getApplicationContext(), "Enter a reasonable number of splitways.",
					   Toast.LENGTH_SHORT).show();
			return;
		}
		
		totalSplitWays = totalSplitWays + 1;
		tipSplitWays=totalSplitWays;

		preUpdateIvButtons();

		tvTotalSplitWays.setText("Split amount " + totalSplitWays +" ways");
		updateTipSplitWaysViews();
		writeParams();
	}
	
	
	private void preUpdateIvButtons() {
		int tmp = totalSplitWays;
		int i;
		for (i=1; i<10; i++) {
			tmp = tmp-10+1;
			if (tmp<(NUM_ROWS*NUM_COLS)+1) {
				break;
			}
		}
		int num10 = i;
		int numextra = tmp;
		
		int total10xButtons = totalSplitWays/((NUM_COLS*NUM_ROWS)+1);
	    if (total10xButtons>0) {
	    	int effectiveSplitWays = totalSplitWays - (total10xButtons*10) + total10xButtons;
			int row = effectiveSplitWays/NUM_COLS;
			int col = effectiveSplitWays%NUM_COLS;
			updateAllIvButtons(num10, numextra, 0);
	    } else {
	    	int tipButtonId = tipSplitWays;
	    	int row = (totalSplitWays)/NUM_COLS;
			int col = (totalSplitWays)%(NUM_COLS);
			
	    	int row1 = (tipSplitWays)/NUM_COLS;
			int col1 = (tipSplitWays)%(NUM_COLS);
			updateAllIvButtons(0, ((row*NUM_COLS)+col), ((row1*NUM_COLS)+col1));
	    }
		
	}
	
	private void updateAllIvButtons(int num10xButtons, int ivButtonId, int ivTipButtonId) {
		
		for (int row=0; row<NUM_ROWS; row++) {							
			for (int col=0; col<NUM_COLS; col++) {
				
				if (ivButtonId > (row*NUM_COLS)+col) {
					ivButtons[row][col].setImageResource(R.drawable.person_black);
				} else {
					ivButtons[row][col].setImageResource(R.drawable.person_grey);
				}
				
				if (((row*NUM_COLS)+col)<num10xButtons) {
					ivButtons[row][col].setImageResource(R.drawable.person_black10);
				}
				
				if (ivTipButtonId > (row*NUM_COLS)+col) {
					ivButtons[row][col].setImageResource(R.drawable.person_orange);
				}
			}
		}
	}
	
	

	public void onDecSplitWays(View v) {
		totalSplitWays = totalSplitWays==1?1:totalSplitWays - 1;
		tipSplitWays=totalSplitWays;

		preUpdateIvButtons();
		
		tvTotalSplitWays.setText("Split amount " + totalSplitWays +" ways");
		updateTipSplitWaysViews();
		writeParams();
	}
	
	public void updateTipSplitWaysViews() {
		tvTipSplitWays.setText("Split tip " + tipSplitWays +" ways");
		sbTipSplitWays.setMax(tipSplitWays-1);
		sbTipSplitWays.setProgress(tipSplitWays-1);
	}
	
    private void readParams() {
    	File filesDir = getFilesDir();
    	File tippingPointParams = new File(filesDir, "tippingPointParams.txt");
    	try {
    		List<String> readLines;
    		readLines = new ArrayList<String>(FileUtils.readLines(tippingPointParams));
    		for (String str : readLines) {
    			String tmp[] = str.split("\t");
    			totalSplitWays = Integer.parseInt(tmp[0]);
    			tipPercent = Integer.parseInt(tmp[1]);
    		}
		} catch (IOException e) {
			totalSplitWays = 1;
			tipPercent = DEF_TIP_PERCENT;		}
	}
    private void writeParams() {
    	File filesDir = getFilesDir();
    	File tippingPointParams = new File(filesDir, "tippingPointParams.txt");
    	try {
    		List<String> writeLines = new ArrayList<String>();
    			String tmp = totalSplitWays+"\t"
    						+tipPercent;
    			writeLines.add(tmp);
			FileUtils.writeLines(tippingPointParams, writeLines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

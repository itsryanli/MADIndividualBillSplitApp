package com.example.splitbillapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView billAmountEditText;
    private TextView numberOfPeopleEditText;
    private Button equalButton;
    private Button customButton;
    private Button combinationButton;
    private Spinner currencySpinner;
    private LinearLayout buttonChoicePercentageAmt;
    private Button bt_percentage;
    private Button bt_amount;
    private TextView totalEqualBreakdown;

    private List<String> historyDataList = new ArrayList<>(); // List to store history data
    private String methodChoice = " ";
    private String choice = " ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        billAmountEditText = findViewById(R.id.billAmountEditText);
        numberOfPeopleEditText = findViewById(R.id.numberOfPeopleEditText);
        currencySpinner = findViewById(R.id.currencySpinner);
        equalButton = findViewById(R.id.equalButton);
        customButton = findViewById(R.id.customButton);
        combinationButton = findViewById(R.id.combinationButton);
        buttonChoicePercentageAmt = findViewById(R.id.buttonChoicePercentageAmt);
        bt_percentage = findViewById(R.id.bt_percentage);
        bt_amount = findViewById(R.id.bt_amount);
        totalEqualBreakdown = findViewById(R.id.totalEqualBreakdown);



        //When click Equal Breakdown, straight display answer and other buttons dissapear
        equalButton.setOnClickListener(v-> {

            buttonChoicePercentageAmt.setVisibility(View.GONE);

            String enteredValue1 = billAmountEditText.getText().toString();
            String enteredValue2 = numberOfPeopleEditText.getText().toString();
            methodChoice = "Equal Breakdown";
            choice = " - ";

            try {
                double billValue = Double.parseDouble(enteredValue1);
                int peopleValue = Integer.parseInt(enteredValue2);


                // Calculate the equal breakdown
                double equalBreakdown = (double) billValue / peopleValue;

                // Get the selected currency from the spinner
                String selectedCurrency = currencySpinner.getSelectedItem().toString();

                // Get the current timestamp
                String timestamp = getCurrentTimestamp();

                // Display the calculated equal breakdown along with the currency
                String equalBreakdownText = "Amount per person: " + equalBreakdown + " " + selectedCurrency;
                totalEqualBreakdown.setText(equalBreakdownText);
                totalEqualBreakdown.setVisibility(View.VISIBLE);

                writeDataToTextFile(timestamp, billValue, peopleValue, equalBreakdown, selectedCurrency, methodChoice, choice);

                // Create the history item string for display
                String historyItem = "Date/Time : " + timestamp +
                        "\nBill Amount : " + billValue +
                        "\nNumber of People : " + peopleValue +
                        "\nAmount of People : " + equalBreakdown +
                        "\nCurrency: " + selectedCurrency +
                        "\nChocie of Breakdown: " + methodChoice +
                        "\nChoice of Calculation: " + choice;

                historyDataList.add(historyItem);

                // Display a toast message with the directory of the data.txt file
                Uri fileUri = Uri.fromFile(new File(getFilesDir(), "data.txt"));
                String filePath = fileUri.getPath();
                Toast.makeText(MainActivity.this, "Data file directory: " + filePath, Toast.LENGTH_LONG).show();

            } catch (NumberFormatException e) {
                // Handle the error
                Toast.makeText(MainActivity.this, "Please enter valid bill amount and number of people!", Toast.LENGTH_LONG).show();
                totalEqualBreakdown.setVisibility(View.GONE);
            }

        });

        //When click on Custom or Combination, percentage amount choice appear
        customButton.setOnClickListener(v -> {
            buttonChoicePercentageAmt.setVisibility(View.VISIBLE);
            totalEqualBreakdown.setVisibility(View.GONE);
        });

        combinationButton.setOnClickListener(v -> {
            buttonChoicePercentageAmt.setVisibility(View.GONE);
            totalEqualBreakdown.setVisibility(View.GONE);
            methodChoice = "Combination Breakdown";

            startCombinationBreakdownActivity(methodChoice);
        });


        bt_percentage.setOnClickListener(v-> {
            choice = "percentage";
            methodChoice = "Custom Breakdown";
            startCustomBreakdownActivity(choice);
        });


        bt_amount.setOnClickListener(v-> {
            choice = "amount";
            methodChoice = "Custom Breakdown";
            startCustomBreakdownActivity(choice);
        });


        //Spinner items
        String[] items = {"Malaysia Ringgit (RM)", "Singapore Dollar (SGD)", "United States Dollar (USD)", "Great Britain Pound (GPB)", "China Yuan (RMB)"};
        //Create adapter and link items as items inside spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //add items into spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

    }

    private void startCustomBreakdownActivity(String choice) {
        String enteredValue1 = billAmountEditText.getText().toString();
        String enteredValue2 = numberOfPeopleEditText.getText().toString();
        String selectedCurrency = currencySpinner.getSelectedItem().toString();

        try {
            double billValue = Double.parseDouble(enteredValue1);
            int peopleValue = Integer.parseInt(enteredValue2);

            Intent customBreakdownIntent = new Intent(MainActivity.this, CustomBreakdown.class);
        customBreakdownIntent.putExtra("billValue", billValue);
        customBreakdownIntent.putExtra("peopleValue", peopleValue);
        customBreakdownIntent.putExtra("choice", choice);
        customBreakdownIntent.putExtra("currency", selectedCurrency); // Pass the selected currency
        customBreakdownIntent.putExtra("methodChoice", methodChoice);


            startActivity(customBreakdownIntent);
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this,"Please enter valid bill amount / number of person!", Toast.LENGTH_LONG).show();
        }
    }

    private void startCombinationBreakdownActivity(String methodChoice) {
        try {
            double billValue = Double.parseDouble(billAmountEditText.getText().toString());
            int peopleValue = Integer.parseInt(numberOfPeopleEditText.getText().toString());

            Intent combinationBreakdownIntent = new Intent(MainActivity.this, CombinationBreakdown.class);
            combinationBreakdownIntent.putExtra("billValue", billValue);
            combinationBreakdownIntent.putExtra("peopleValue", peopleValue);
            combinationBreakdownIntent.putExtra("methodChoice", methodChoice);

            startActivity(combinationBreakdownIntent);
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Please enter valid bill amount / number of people!", Toast.LENGTH_LONG).show();
        }
    }



    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }


    private void writeDataToTextFile(String timestamp, double billValue, int peopleValue, double equalBreakdown, String currency, String methodChoice, String choice) {
        String data = "Date/Time : " + timestamp +
                "\nBill Amount : " + billValue +
                "\nNumber of People : " + peopleValue +
                "\nAmount of People : " + equalBreakdown +
                "\nCurrency: " + currency +
                "\nChoice of Breakdown: "+ methodChoice +
                "\nChoice of Calculation: "+ choice + "\n\n";
        try {
            FileOutputStream fileOutputStream = openFileOutput("data.txt", Context.MODE_APPEND);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_history) {
            // Start the HistoryActivity when the history menu item is clicked
            Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(historyIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
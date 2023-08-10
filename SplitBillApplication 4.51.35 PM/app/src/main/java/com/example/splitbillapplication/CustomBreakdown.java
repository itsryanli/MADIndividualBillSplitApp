package com.example.splitbillapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CustomBreakdown extends AppCompatActivity {

    private double remainingAmount;
    private ArrayList<LinearLayout> personLayouts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_breakdown);

        Intent getIntent = getIntent();

        double billValue = getIntent().getDoubleExtra("billValue", 0.0);
        int peopleValue = getIntent().getIntExtra("peopleValue", 0);
        String choice = getIntent().getStringExtra("choice");
        String currency = getIntent().getStringExtra("currency");
        String methodChoice = getIntent.getStringExtra("methodChoice");
        LinearLayout layout = findViewById(R.id.editTextContainer);

        for (int i = 0; i < peopleValue; i++) {
            LinearLayout personLayout = new LinearLayout(this);
            personLayouts.add(personLayout);
            layout.addView(personLayout);

            // Create an EditText for person's name
            EditText personNameEditText = new EditText(this);
            personNameEditText.setHint("Enter name for person " + (i + 1));
            personLayout.addView(personNameEditText);

            // Create an EditText for percentage/ratio/amount input
            EditText customInputEditText = new EditText(this);
            customInputEditText.setHint("Enter " + choice + " for person " + (i + 1));
            personLayout.addView(customInputEditText);

            // Add a line separator between each person's input group
            View separator = new View(this);
            separator.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2));
            separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            personLayout.addView(separator);
        }


        StringBuilder historyData = new StringBuilder();
        Button calculateButton = new Button(this);
        calculateButton.setText("Calculate and Save");
        layout.addView(calculateButton);

        calculateButton.setOnClickListener(v -> {
            boolean inputError = false;
            double customInputTotal = 0.0; // Initialize the total custom input amount

            for (int i = 0; i < personLayouts.size(); i++) {
                EditText personNameEditText = (EditText) personLayouts.get(i).getChildAt(0);
                EditText customInputEditText = (EditText) personLayouts.get(i).getChildAt(1);

                String personName = personNameEditText.getText().toString().trim();
                String inputText = customInputEditText.getText().toString().trim();

                if (personName.isEmpty() || inputText.isEmpty()) {
                    Toast.makeText(CustomBreakdown.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    inputError = true;
                    break;
                }

                double customInput = Double.parseDouble(inputText);
                customInputTotal += customInput; // Accumulate the custom input amounts

                double calculatedValue = calculateValue(choice, customInput, billValue);

                String timestamp = getCurrentTimestamp();

                String historyItem = "Date/Time: " + timestamp +
                        "\nPerson's Name: " + personName +
                        "\nBill Amount: " + billValue +
                        "\n" + choice + ": " + customInput +
                        "\nCalculated Value: " + calculatedValue +
                        "\nCurrency: " + currency +
                        "\nChoice of Breakdown: " + methodChoice +
                        "\nChoice of Calculation: " + choice + "\n\n";

                historyData.append(historyItem);
            }

            if (choice.equals("amount")) {
                if (Math.abs(customInputTotal - billValue) > 0.01) {
                    Toast.makeText(CustomBreakdown.this, "Individual amounts do not match the total bill amount", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!inputError) {
                writeDataToTextFile(historyData.toString()); // Pass the complete history data to the method
                Toast.makeText(CustomBreakdown.this, "Data saved to data.txt", Toast.LENGTH_LONG).show();
            }
        });



        Button sendToWhatsAppButton = new Button(this);
        sendToWhatsAppButton.setText("Send to WhatsApp");
        layout.addView(sendToWhatsAppButton);

        sendToWhatsAppButton.setOnClickListener(v -> {
            // Get the history data that you want to send
            String historyDataToSend = historyData.toString(); // Replace with the correct data source

            // Create an Intent to share the data via WhatsApp
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, historyDataToSend);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp"); // WhatsApp package name

            // Check if WhatsApp is installed on the device
            if (sendIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(sendIntent);
            } else {
                // WhatsApp is not installed
                Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Perform calculation based on choice and deduct the calculated amount from the billValue
    private double calculateValue(String choice, double customInput,double remainingAmount) {
        double calculatedValue = 0.0;

        switch (choice) {
            case "percentage":
                calculatedValue = remainingAmount * (customInput / 100);
                break;
            case "amount":
                calculatedValue = customInput;
                break;
        }

        // Deduct the calculated value from the remainingAmount
        remainingAmount -= calculatedValue;

        return calculatedValue;
    }

    private void writeDataToTextFile(String data) {
        try {
            FileOutputStream fileOutputStream = openFileOutput("data.txt", MODE_APPEND);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

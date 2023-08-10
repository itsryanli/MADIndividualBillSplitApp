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
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CombinationBreakdown extends AppCompatActivity {

    private LinearLayout personInputContainer;
    private Button btnCalculate;
    private Button btnShare;

    private int billValue;
    private int peopleValue;

    private void writeDataToTextFile(String data) {
        try {
            FileOutputStream fileOutputStream = openFileOutput("data.txt", MODE_APPEND);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combination_breakdown);

        personInputContainer = findViewById(R.id.editTextContainer);

        Intent getIntent = getIntent();
        billValue = getIntent.getIntExtra("billValue", 0);
        peopleValue = getIntent.getIntExtra("peopleValue", 0);

        setupPersonInputFields(peopleValue);


        Button calculate = new Button(this);
        StringBuilder historyData = new StringBuilder();
        calculate.setOnClickListener(v-> {
            for (int i = 0; i < personInputContainer.getChildCount(); i++) {
                View personView = personInputContainer.getChildAt(i);
                EditText etPersonName = personView.findViewById(R.id.et_person_name);
                EditText etPersonPercentage = personView.findViewById(R.id.et_person_percentage);
                EditText etPersonAmount = personView.findViewById(R.id.et_person_amount);

                String personName = etPersonName.getText().toString().trim();
                String percentageInput = etPersonPercentage.getText().toString().trim();
                String amountInput = etPersonAmount.getText().toString().trim();

                if (personName.isEmpty() || percentageInput.isEmpty() || amountInput.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields for person " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                double percentage = parseDouble(percentageInput);
                double amount = parseDouble(amountInput);

                double calculatedValue = (percentage / 100) * billValue;
                if (Math.abs(calculatedValue - amount) > 0.01) {
                    Toast.makeText(this, "Percentage and amount values do not match for person " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                String timestamp = getCurrentTimestamp();

                String historyItem = "Date/Time: " + timestamp +
                        "\nPerson's Name: " + personName +
                        "\nBill Amount: " + billValue +
                        "\nPercentage: " + percentage +
                        "\nAmount: " + amount +
                        "\nCalculated Value: " + calculatedValue +
                        "\n\n";

                historyData.append(historyItem);
            }

            writeDataToTextFile(historyData.toString()); // Pass the complete history data to the method
            Toast.makeText(this, "Data saved to data.txt", Toast.LENGTH_LONG).show();
        });
        calculate.setText("Calculate and Save");
        personInputContainer.addView(calculate);

        Button sendToWhatsAppButton = new Button(this);
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
        sendToWhatsAppButton.setText("Send to WhatsApp");
        personInputContainer.addView(sendToWhatsAppButton);
    }

    private void setupPersonInputFields(int numPeople) {
        personInputContainer.removeAllViews(); // Clear any existing views

        for (int i = 0; i < numPeople; i++) {
            View personView = getLayoutInflater().inflate(R.layout.person_input_item, null);
            EditText etPersonName = personView.findViewById(R.id.et_person_name);
            EditText etPersonPercentage = personView.findViewById(R.id.et_person_percentage);
            EditText etPersonAmount = personView.findViewById(R.id.et_person_amount);

            etPersonPercentage.addTextChangedListener(personInputTextWatcher(etPersonPercentage, etPersonAmount));
            etPersonAmount.addTextChangedListener(personInputTextWatcher(etPersonAmount, etPersonPercentage));

            personInputContainer.addView(personView);
        }
    }

    private TextWatcher personInputTextWatcher(EditText sourceEditText, EditText targetEditText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String inputText = editable.toString().trim();
                double inputValue = parseDouble(inputText);
                double calculatedValue;

                if (sourceEditText.getId() == R.id.et_person_percentage) {
                    calculatedValue = (inputValue / 100) * billValue;
                    targetEditText.setText(String.format("%.2f", calculatedValue));
                } else {
                    calculatedValue = (inputValue / billValue) * 100;
                    targetEditText.setText(String.format("%.2f", calculatedValue));
                }
            }
        };
    }


}

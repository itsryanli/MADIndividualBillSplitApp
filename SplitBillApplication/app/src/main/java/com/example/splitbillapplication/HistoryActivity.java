package com.example.splitbillapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private ArrayAdapter<String> historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.historyListView);

        List<String> historyData = readDataFromTextFile();
        historyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyData);
        historyListView.setAdapter(historyAdapter);

        // Add click listener to ListView items
        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            String historyItemText = historyAdapter.getItem(position);
            showSendToWhatsAppPopup(historyItemText);
        });
    }

    private List<String> readDataFromTextFile() {
        List<String> historyData = new ArrayList<>();

        try {
            FileInputStream fileInputStream = openFileInput("data.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            StringBuilder historyItem = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty()) {
                    historyData.add(historyItem.toString()); // Add the completed history item
                    historyItem = new StringBuilder(); // Reset for the next history item
                } else {
                    historyItem.append(line).append("\n");
                }
            }


            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return historyData;
    }
    private void showSendToWhatsAppPopup(String historyItemText) {
        // Create and show a dialog/popup here
        // You can use AlertDialog, DialogFragment, or any other approach you prefer
        // Inside the dialog, you can provide options like "Send to WhatsApp" and "Cancel"
        // Handle the click events accordingly
        // For simplicity, here's an example using AlertDialog:

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send to WhatsApp")
                .setMessage("Do you want to send this history to WhatsApp?")
                .setPositiveButton("Send", (dialog, which) -> {
                    sendToWhatsApp(historyItemText);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }


    private void sendToWhatsApp(String historyItemText) {
        // Create an Intent to share the data via WhatsApp
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, historyItemText);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp"); // WhatsApp package name

        // Check if WhatsApp is installed on the device
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(sendIntent);
        } else {
            // WhatsApp is not installed
            Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
        }
    }


}

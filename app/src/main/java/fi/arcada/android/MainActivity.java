package fi.arcada.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //Linje diagrammet
    GraphView graph;
    // Två texfield frö from>to
    EditText editTextDate;
    EditText editTextDate2;
    //EditText editTextCurrency;
    Spinner spinner;
    //knappar för fönsterstorlek
    Button btn, btn2, btn3;
    /* en arrayList i vilken växelkursvärdena sparas */
    ArrayList<Double> currencyValues;
    String from = "2015-02-05";//YY-MM-DD
    String to = "2021-03-16";
    String currency = "SEK";
    int window; //fönsterstorlek

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Android studio för att sätta landscape som default vid start av appen
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //editTextCurrency = findViewById(R.id.editTextCurrency);
        editTextDate = findViewById(R.id.editTextDate);
        editTextDate2 = findViewById(R.id.editTextDate2);
        // https://www.youtube.com/watch?v=on_OrrX7Nw4 från slidsen
        spinner = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this , R.array.currencyArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        btn = findViewById(R.id.button);
        btn2 = findViewById(R.id.button2);
        btn3 = findViewById(R.id.button3);

        //hittar grafen
        graph = findViewById(R.id.graph);


    }

    public void buttonClick(View view) {
        if (view.getId() == R.id.button) window = 4;
        if (view.getId() == R.id.button2) window = 50;
        if (view.getId() == R.id.button3) window = 200;
        from = editTextDate.getText().toString();
        to = editTextDate2.getText().toString();
        //currency = editTextCurrency.getText().toString();

        currencyValues = getCurrencyValues();
        buildGraph(graph, currencyValues);
        buildGraph2(graph, Statistics.movingAverage(currencyValues, window));

    }

    // bygger grafen som linjediagram
    public void buildGraph(GraphView graph, ArrayList<Double> dataset) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

        series.setColor(Color.BLUE);
        for (int i = 0; i < dataset.size(); i++) {
            series.appendData(new DataPoint(i, dataset.get(i)), true, dataset.size());
        }
        graph.removeAllSeries();
        graph.addSeries(series);
    }

    public void buildGraph2(GraphView graph, ArrayList<Double> dataset) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        series.setColor(Color.GREEN);

        for (int i = 0; i < dataset.size(); i++) {
            series.appendData(new DataPoint(i+window, dataset.get(i)), true, dataset.size());
        }
        graph.addSeries(series);
    }

    /* Färdig metod som hämtar växelkurserna från CurrencyApi klassen */
    public ArrayList<Double> getCurrencyValues() {

        CurrencyApi api = new CurrencyApi();
        ArrayList<Double> currencyData = null;
      /*  System.out.println(String.format("https://api.exchangeratesapi.io/history?start_at=%s&end_at=%s&symbols=%s",
                from.trim(),
                to.trim(),
                currency.trim()
        ));*/
        try {
            //https://api.exchangeratesapi.io/history?start_at=%s&end_at=%s&symbols=%s Gamla API funkar ej mera.
            String jsonData = api.execute(String.format("https://api.exchangerate.host/timeseries?start_date=%s&end_date=%s&symbols=%s",
                    from.trim(),
                    to.trim(),
                    currency.trim()
            )).get();

            if (jsonData != null) {
                currencyData = api.getCurrencyData(jsonData, currency.trim());
                Toast.makeText(getApplicationContext(), String.format("Hämtade %s valutakursvärden från servern", currencyData.size()), Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Kunde inte hämta växelkursdata från servern: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return currencyData;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currency = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
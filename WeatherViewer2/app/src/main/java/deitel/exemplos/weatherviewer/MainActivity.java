package deitel.exemplos.weatherviewer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;



//Atividade que gerencia a interface gráfica de usuário, requisita e processa as requisições para o web service.


public class MainActivity extends AppCompatActivity {

    private List<Weather> weatherList = new ArrayList<>();
    private WeatherArrayAdapter weatherArrayAdapter;
    private ArrayAdapter<Weather> array;
    private ListView weatherListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        weatherListView = (ListView) findViewById(R.id.weatherListview);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        array = new ArrayAdapter<Weather>(this, R.layout.list_item);
        weatherListView.setAdapter(weatherArrayAdapter);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText locationEditText = (EditText) findViewById(R.id.locationEditText);
                URL url = createUrl(locationEditText.getText().toString());
                if(url != null){

                    dismissKeyboard(locationEditText);
                    GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                    getLocalWeatherTask.execute(url);
                }else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url, Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    private void dismissKeyboard(View view){

        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    private URL createUrl(String city){

        String apiKey = getString(R.string.api_key); // Minha chave de api
        String baseUrl = getString(R.string.web_service_url);

        try{
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") +
                    "&units=imperial&cnt=15&APPID=" + apiKey; //Minah chave de api não permite carrregar os dados para 16 dias
                                                            //pois é a versão gratuita, então deixei o padrão 7 dias.

            return new URL(urlString);
        }catch (Exception e){

            e.printStackTrace();
        }

        return null;

    }

    //Se conecta ao web service requisita as iformações sobre o clima para 7 dias e processas as informações recebidas
    //As informações recebidas vem no formato JSON e precisam ser recuperadas e formatadas para melhor manipulação no software
    //por isso são passadas para um ArrayList, weatherList.
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(URL... params) {

            HttpURLConnection connection = null;

            try{

                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if(response == HttpURLConnection.HTTP_OK){

                    StringBuilder builder = new StringBuilder();


                    try(BufferedReader reader = new BufferedReader(
                           new InputStreamReader(connection.getInputStream()))){

                        String line;

                        while((line = reader.readLine()) != null){


                                builder.append(line);

                        }

                        JSONObject jsonObject = new JSONObject(builder.toString());
                        return  jsonObject;

                    }catch(Exception e){

                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }



                }else{
                    Snackbar.make(findViewById(R.id.coordinatorLayout),R.string.connect_error, Snackbar.LENGTH_LONG)
                            .show();
                }

            } catch (Exception e){

                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connect_error, Snackbar.LENGTH_LONG)
                        .show();
                e.printStackTrace();

            }finally {

                connection.disconnect();
            }

            return null;
        }

        protected void onPostExecute(JSONObject object){


            convertJSONtoArrayList(object);
            weatherArrayAdapter.notifyDataSetChanged();
            weatherListView.smoothScrollToPosition(0);



        }
    }

    //Recupera as informações de um objeto JSON e as armazenas em um arraylist.
    private void convertJSONtoArrayList(JSONObject forecast){

        weatherList.clear();


        try{

            JSONArray list = forecast.getJSONArray("list");

            for(int i = 0; i < list.length(); i++){

                JSONObject day = list.getJSONObject(i);


                JSONObject temperatures = day.getJSONObject("main");

                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                Weather weather1 = new Weather(

                        day.getLong("dt"),
                        temperatures.getDouble("temp_min"),
                        temperatures.getDouble("temp_max"),
                        temperatures.getInt("humidity"),
                        weather.getString("description"),
                        weather.getString("icon"));

                weatherList.add(weather1);

            }


        }catch (JSONException js){


            js.printStackTrace();
        }
    }
}

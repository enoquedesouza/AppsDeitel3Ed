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

    private List<Weather> weatherList = new ArrayList<>(); //lista de objetos weather

    //Adaptador para o ListView. fornece um Item view personalizado com as iformações climáticas de um dia.
    private WeatherArrayAdapter weatherArrayAdapter;

    //ListView para exibir as informações  climáticas obtidas do web service
    private ListView weatherListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        weatherListView = (ListView) findViewById(R.id.weatherListview); // incializa o ListView

        // Define o adaptador com uma ista de objetos Weather
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);

        //Seta o adaptador do ListView
        weatherListView.setAdapter(weatherArrayAdapter);


        // Inicializa o floating button (fica suspenso na tela próximo a caixa de texto)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Inicializa a caixa de texto onde o usuário digitará o local do qual ele deseja informações
                EditText locationEditText = (EditText) findViewById(R.id.locationEditText);

                //Configura um o objeto URL utilizado para se conecatar ao web service
                URL url = createUrl(locationEditText.getText().toString());

                if(url != null){

                    //Oculta o teclado
                    dismissKeyboard(locationEditText);

                    //Cria uma tarefa assíncrona para a conexão e dowloada das iformações climáticas
                    GetWeatherTask getLocalWeatherTask = new GetWeatherTask();

                    // Executa a terefa
                    getLocalWeatherTask.execute(url);

                }else {

                    // Se ocorrer algum problema na criação do URL
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url, Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    // @param view, view de referência do floating button
    private void dismissKeyboard(View view){

        // recupera uma intância de InputMethodManager para acessar o serviço de teclado
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        //Esconde o teclado
        im.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    // @param city, nome da cidade fornecida pelo usuário da qual ele deseja as informações climáticas
    private URL createUrl(String city){


        // Chave de API necessária para acessar os serviço de forncimento de informações do clima
        // fornecidos por https://openweathermap.org
        String apiKey = getString(R.string.api_key);

        String baseUrl = getString(R.string.web_service_url);//endereço base do web service

        try{

            //Endereço para a requisição do serviço
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") +
                    "&units=imperial&cnt=15&APPID=" + apiKey;

            // URL para conexão ao web service e requisição do serviço
            return new URL(urlString);


        }catch (Exception e){

            e.printStackTrace();
        }

        return null;

    }

    //AsyncTask para a conexão com o web service e download das informações.
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject>{

        // Executa a tarefa em plano de fundo
        //@param <tipo> ... params é um array de tamanho variável onde  tipo depende da tarfa a ser executada e é passado
        //na chamada de execute
        @Override
        protected JSONObject doInBackground(URL... params) {

            HttpURLConnection connection = null; // cria um HttpURLConnection para uma conexão HTTP

            try{


                //Abre a conexão com o endereo URL passado como parâmetro
                connection = (HttpURLConnection) params[0].openConnection();

                int response = connection.getResponseCode(); //Recupera a resposta do servidor

                //Se statur HTTP_OK, ou seja os dados foram enviados
                if(response == HttpURLConnection.HTTP_OK){

                    StringBuilder builder = new StringBuilder(); //Deine um construtor de strings


                    // Cria um Buffer para a leitura dos streams enviado pelo servidor
                    try(BufferedReader reader = new BufferedReader(

                           new InputStreamReader(connection.getInputStream()))){

                        String line;

                        //Enquanto ouver strings para serem lidas no stream
                        while((line = reader.readLine()) != null){

                                builder.append(line); //Junte a linha com a linha anterioir para forma a string

                        }

                        // Cria um objeto JSON com a string contruida a partir dos dados enviados pelo servidor
                        JSONObject jsonObject = new JSONObject(builder.toString());

                        //Retorna o objeto para ser utilizado na GUI
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

        //@param, object, objeto JSON passado como parâmetro pela AsyncTask
        //este método realiza suas operações na GUI
        protected void onPostExecute(JSONObject object){


            convertJSONtoArrayList(object); //Adiciona as informações de interesse do objeto JSON em ArrayList

            weatherArrayAdapter.notifyDataSetChanged(); //Notifica o ListView que os dados mudaram e precisa ser atualizado

            weatherListView.smoothScrollToPosition(0); // Rola o ListView para a primeira posição da lista



        }
    }

    //@param forecast, objeto JSON passado pela AsyncTask. Nele contém as informações enviada pelo web service

    private void convertJSONtoArrayList(JSONObject forecast){

        weatherList.clear(); // limpa a lista de weathers


        try{

            JSONArray list = forecast.getJSONArray("list"); //recupera um array de JSONs onde estão as informações de interesse

            for(int i = 0; i < list.length(); i++){

                JSONObject day = list.getJSONObject(i);


                //Recuepra o objeto JSON no qual estão as informações de temperatura
                JSONObject temperatures = day.getJSONObject("main");

                //Recuepra o objeto JSON no qual estão a descrição do tempo e icone que representa as condições
                //climáticas
                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                //cria um objeto weather que armazena as condições climáticas d eum dia.
                Weather weather1 = new Weather(

                        day.getLong("dt"),
                        temperatures.getDouble("temp_min"),
                        temperatures.getDouble("temp_max"),
                        temperatures.getInt("humidity"),
                        weather.getString("description"),
                        weather.getString("icon"));

                //Adiciona este objeto na lista
                weatherList.add(weather1);

            }


        }catch (JSONException js){


            js.printStackTrace();
        }
    }
}
